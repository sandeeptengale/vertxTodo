import io.vertx.core.Vertx;
import service.RxTodoVerticle;

public class Runner {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new RxTodoVerticle());
    }
}
