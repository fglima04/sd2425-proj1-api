package fctreddit.clients.grpc;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.grpc.Channel;
import io.grpc.LoadBalancerRegistry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.internal.PickFirstLoadBalancerProvider;
import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.clients.java.UsersClient;
import fctreddit.discovery.Discovery;
import fctreddit.impl.server.grpc.generated_java.UsersGrpc;
import fctreddit.impl.server.grpc.generated_java.UsersProtoBuf.CreateUserArgs;
import fctreddit.impl.server.grpc.generated_java.UsersProtoBuf.CreateUserResult;
import fctreddit.impl.server.grpc.generated_java.UsersProtoBuf.GetUserArgs;
import fctreddit.impl.server.grpc.generated_java.UsersProtoBuf.GetUserResult;
import fctreddit.impl.server.grpc.generated_java.UsersProtoBuf.GrpcUser;
import fctreddit.impl.server.grpc.generated_java.UsersProtoBuf.SearchUserArgs;
import fctreddit.impl.grpc.util.DataModelAdaptor;

public class GrpcUsersClient extends UsersClient {

	static {
		LoadBalancerRegistry.getDefaultRegistry().register(new PickFirstLoadBalancerProvider());
	}

	private static final String SERVICE_NAME = "UsersService";


	private final Discovery discovery;
	private ManagedChannel channel;
	private UsersGrpc.UsersBlockingStub stub;

	public GrpcUsersClient(Discovery discovery) {
		this.discovery = discovery;
		updateChannelAndStub();
	}

	private void updateChannelAndStub() {
		URI serverURI = resolveServerURI();
		if (channel != null) {
			channel.shutdownNow();
		}
		channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort())
				.usePlaintext()
				.build();
		stub = UsersGrpc.newBlockingStub(channel).withDeadlineAfter(READ_TIMEOUT, TimeUnit.MILLISECONDS);
	}

	private URI resolveServerURI() {
		URI[] uris = discovery.knownUrisOf(SERVICE_NAME, 1);
		if (uris.length == 0) {
			throw new RuntimeException("No available servers for service: " + SERVICE_NAME);
		}
		return uris[0];
	}

	@Override
	public Result<String> createUser(User user) {
		try {
			CreateUserResult res = stub.createUser(CreateUserArgs.newBuilder()
					.setUser(DataModelAdaptor.User_to_GrpcUser(user))
					.build());
			return Result.ok(res.getUserId());
		} catch (StatusRuntimeException sre) {
			if (sre.getStatus().getCode() == Status.Code.UNAVAILABLE) {
				updateChannelAndStub();
			}
			return Result.error(statusToErrorCode(sre.getStatus()));
		}
	}

	@Override
	public Result<User> getUser(String userId, String password) {
		try {
			GetUserResult res = stub.getUser(GetUserArgs.newBuilder()
					.setUserId(userId).setPassword(password)
					.build());
			return Result.ok(DataModelAdaptor.GrpcUser_to_User(res.getUser()));
		} catch (StatusRuntimeException sre) {
			if (sre.getStatus().getCode() == Status.Code.UNAVAILABLE) {
				updateChannelAndStub();
			}
			return Result.error(statusToErrorCode(sre.getStatus()));
		}
	}

	@Override
	public Result<User> updateUser(String userId, String pwd, User user) {
		throw new RuntimeException("Not Implemented...");
	}

	@Override
	public Result<User> deleteUser(String userId, String pwd) {
		throw new RuntimeException("Not Implemented...");
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		try {
			Iterator<GrpcUser> res = stub.searchUsers(SearchUserArgs.newBuilder()
					.setPattern(pattern)
					.build());

			List<User> ret = new ArrayList<>();
			while (res.hasNext()) {
				ret.add(DataModelAdaptor.GrpcUser_to_User(res.next()));
			}
			return Result.ok(ret);
		} catch (StatusRuntimeException sre) {
			if (sre.getStatus().getCode() == Status.Code.UNAVAILABLE) {
				updateChannelAndStub();
			}
			return Result.error(statusToErrorCode(sre.getStatus()));
		}
	}

	static ErrorCode statusToErrorCode(Status status) {
		return switch (status.getCode()) {
			case OK -> ErrorCode.OK;
			case NOT_FOUND -> ErrorCode.NOT_FOUND;
			case ALREADY_EXISTS -> ErrorCode.CONFLICT;
			case PERMISSION_DENIED -> ErrorCode.FORBIDDEN;
			case INVALID_ARGUMENT -> ErrorCode.BAD_REQUEST;
			case UNIMPLEMENTED -> ErrorCode.NOT_IMPLEMENTED;
			default -> ErrorCode.INTERNAL_ERROR;
		};
	}
}
