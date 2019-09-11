package common;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.CorsHandler;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public abstract class RestfulApiVerticle extends AbstractVerticle {
    protected Completable createHttpServer(Router router, String host, int port) {
        return vertx.createHttpServer()
                .requestHandler(router)
                .rxListen(port, host).ignoreElement();
    }

    protected void created(RoutingContext context) {
        context.response().setStatusCode(201).end();
    }

    protected void created(RoutingContext context, String content) {
        context.response().setStatusCode(201).putHeader("content-type", "application/json")
                .end(content);
    }

    protected void badRequest(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(400).putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
    }

    protected void badRequest(RoutingContext context, String cause) {
        context.response().setStatusCode(400).putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", cause).encodePrettily());
    }

    protected void badRequest(RoutingContext context) {
        context.response().setStatusCode(400).end();
    }

    protected void ok(RoutingContext context, String content) {
        context.response().setStatusCode(200).putHeader("content-type", "application/json")
                .end(content);
    }

    protected void ok(RoutingContext context) {
        context.response().setStatusCode(200).end();
    }

    protected void enableCorsSupport(Router router) {
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");
        router.route().handler(CorsHandler.create("*")
            .allowedHeaders(allowHeaders)
            .allowedMethod(HttpMethod.GET)
            .allowedMethod(HttpMethod.POST)
            .allowedMethod(HttpMethod.DELETE)
            .allowedMethod(HttpMethod.PUT));
    }

    protected void sendResponse(RoutingContext context, Completable asyncResult) {
        HttpServerResponse response = context.response();
        if (Objects.isNull(asyncResult)) internalError(context, "invalid_status");
        else asyncResult.subscribe(response::end, ex -> internalError(context, ex));
    }

    protected void sendResponse(RoutingContext context, Completable asyncResult, Consumer<RoutingContext> f) {
        if (Objects.isNull(asyncResult)) internalError(context, "invalid_status");
        else asyncResult.subscribe(() -> f.accept(context), ex -> internalError(context, ex));
    }

    protected <T> void sendResponse(RoutingContext context, Single<T> asyncResult, Function<T, String> converter, BiConsumer<RoutingContext, String> f) {
        if (Objects.isNull(asyncResult)) internalError(context, "invalid_status");
        else asyncResult.subscribe(r -> f.accept(context, converter.apply(r)), ex -> internalError(context, ex));
    }

    protected <T> void sendResponse(RoutingContext context, Single<T> asyncResult, Function<T, String> converter) {
        if (Objects.isNull(asyncResult)) internalError(context, "invalid_status");
        else asyncResult.subscribe(r -> ok(context, converter.apply(r)), ex -> internalError(context, ex));
    }

    protected <T> void sendResponse(RoutingContext context, Maybe<T> asyncResult, Function<T, String> converter) {
        if (Objects.isNull(asyncResult)) internalError(context, "invalid_status");
        else {
            Single<Optional<T>> single = asyncResult.map(Optional::of)
                    .switchIfEmpty(Maybe.just(Optional.empty()))
                    .toSingle();
            //sendResponse(context, single, converter);
        }
    }

    protected <T> void sendResponseOpt(RoutingContext context, Single<Optional<T>> asyncResult, Function<T, String> converter) {
        if (Objects.isNull(asyncResult)) internalError(context, "invalid_status");
        else {
            asyncResult.subscribe(r -> {
                if (r.isPresent()) {
                    ok(context, converter.apply(r.get()));
                } else {
                    notFound(context);
                }
            }, ex -> internalError(context, ex));
        }
    }

    private void internalError(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
    }

    private void internalError(RoutingContext context, String cause) {
        context.response().setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", cause).encodePrettily());
    }

    private void notFound(RoutingContext context) {
        context.response().setStatusCode(404)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("message", "not_found").encodePrettily());
    }
}
