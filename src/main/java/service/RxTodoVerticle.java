package service;

import common.Constants;
import common.RestfulApiVerticle;
import io.reactivex.Completable;
import io.vertx.core.Future;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.redis.RedisOptions;
import model.Todo;

import java.util.Objects;

public class RxTodoVerticle extends RestfulApiVerticle {
    private static final Logger logger = LoggerFactory.getLogger(RxTodoVerticle.class);
    private static String HOST = "127.0.0.1";
    private static int PORT = 8082;

    private TodoService todoService;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        enableCorsSupport(router);

        router.get(Constants.GET_ALL).handler(this::handleGetAll);
        router.get(Constants.GET_API).handler(this::handleGetById);
        router.post(Constants.CREATE).handler(this::handleCreate);
        router.delete(Constants.DELETE_ALL).handler(this::handleDeleteAll);
        router.delete(Constants.DELETE).handler(this::handleDeleteById);

        String host = config().getString("http.address", HOST);
        int port = config().getInteger("http.port", PORT);

        initService().andThen(createHttpServer(router, host, port))
                .subscribe(startFuture::complete, startFuture::fail);
    }

    private Completable initService() {
        RedisOptions config = new RedisOptions().setHost(config().getString("redis.host", "127.0.0.1"))
                .setPort(config().getInteger("redis.port", 6379));
        todoService = new RedisTodoService(vertx, config);
        return todoService.initData();
    }

    private void handleDeleteById(RoutingContext context) {
        String todoId = context.request().getParam("todoId");
        sendResponse(context, todoService.delete(todoId), Json::encodePrettily);
    }

    private void handleDeleteAll(RoutingContext context) {
        sendResponse(context, todoService.deleteAll(), Json::encodePrettily);
    }

    private void handleCreate(RoutingContext context) {
        try {
            JsonObject jsonObject = context.getBodyAsJson();
            if (jsonObject != null) {
                Todo todo = wrapObject(new Todo(jsonObject), context);
                sendResponse(context, todoService.insert(todo), Json::encodePrettily, this::created);
                return;
            }
            badRequest(context);
        } catch (DecodeException ex) {
            badRequest(context, ex);
        }
    }

    private void handleGetById(RoutingContext context) {
        String todoId = context.request().getParam("todoId");
        if (Objects.isNull(todoId)) {
            badRequest(context, "Invalid todoId");
            return;
        }
        sendResponse(context, todoService.getTodoById(todoId), Json::encodePrettily);
    }

    private void handleGetAll(RoutingContext context) {
        sendResponse(context, todoService.getAll(), Json::encodePrettily);
    }

    private Todo wrapObject(Todo todo, RoutingContext context) {
        int id = todo.getId();
        if (id > Todo.getAccId()) {
            Todo.setAccId(id);
        } else if (id == 0) {
            todo.setId();
            todo.setUrl(context.request().absoluteURI() + "/" + todo.getId());
        }
        return todo;
    }
}
