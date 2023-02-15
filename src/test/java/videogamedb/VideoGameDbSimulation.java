package videogamedb;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class VideoGameDbSimulation extends Simulation {

    // Http Configuration
    private HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    // HTTP CALLS
    private static ChainBuilder getAllVideoGames =
            exec(http("Get all video games")
                    .get("/videogame"));

    private static ChainBuilder authenticate =
            exec(http("Authenticate")
                    .post("/authenticate")
                    .body(StringBody("{\n" +
                            "  \"password\": \"admin\",\n" +
                            "  \"username\": \"admin\"\n" +
                            "}"))
                    .check(jmesPath("token").saveAs("jwtToken")));

    private static ChainBuilder createNewGame =
            exec(http("Create New Game")
                    .post("/videogame")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .body(ElFileBody("bodies/newGameTemplate.json")).asJson());

    private static ChainBuilder getLastPostedGame =
            exec(http("Get Last Posted Game")
                    .get("/videogame/1"));

    private static ChainBuilder deleteLastPostedGame =
            exec(http("Delete Game")
                    .delete("/videogame/1")
                    .header("Authorization", "Bearer #{jwtToken}"));

    // Scenario Definition
    // 1. Get all video games
    // 2. Authenticate with API
    // 3. Create a new game
    // 4. Get details of newly created game
    // 5. Delete newly created game
    private ScenarioBuilder scn = scenario("Video Game DB Stress Test")
            .exec(getAllVideoGames)
            .pause(2)
            .exec(authenticate)
            .pause(2)
            .exec(createNewGame)
            .pause(2)
            .exec(getLastPostedGame)
            .pause(2)
            .exec(deleteLastPostedGame);

    // Load Simulation
    {
        setUp(
                scn.injectOpen(atOnceUsers(1))
        ).protocols(httpProtocol);
    }
}
