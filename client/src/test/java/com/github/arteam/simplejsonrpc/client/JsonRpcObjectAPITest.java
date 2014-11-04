package com.github.arteam.simplejsonrpc.client;

import com.github.arteam.simplejsonrpc.client.domain.Player;
import com.github.arteam.simplejsonrpc.client.domain.Position;
import com.github.arteam.simplejsonrpc.client.domain.Team;
import com.github.arteam.simplejsonrpc.client.exception.JsonRpcException;
import com.github.arteam.simplejsonrpc.client.object.FixedIntegerIdGenerator;
import com.github.arteam.simplejsonrpc.client.object.FixedStringIdGenerator;
import com.github.arteam.simplejsonrpc.client.object.TeamService;
import com.github.arteam.simplejsonrpc.core.domain.ErrorMessage;
import com.google.common.base.Optional;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Date: 24.08.14
 * Time: 18:06
 *
 * @author Artem Prigoda
 */
public class JsonRpcObjectAPITest extends BaseClientTest {

    @Test
    public void testAddPlayer() {
        JsonRpcClient client = initClient("add_player");
        TeamService teamService = client.onDemand(TeamService.class, new FixedStringIdGenerator("asd671"));
        boolean result = teamService.add(new Player("Kevin", "Shattenkirk", new Team("St. Louis Blues", "NHL"), 22, Position.DEFENDER,
                ISODateTimeFormat.date().withZone(DateTimeZone.UTC).parseDateTime("1989-01-29").toDate(),
                4.25));
        assertThat(result).isTrue();
    }

    @Test
    public void findPlayerByInitials() {
        JsonRpcClient client = initClient("find_player");
        Player player = client.onDemand(TeamService.class, new FixedIntegerIdGenerator(43121)).findByInitials("Steven", "Stamkos");
        assertThat(player).isNotNull();
        assertThat(player.getFirstName()).isEqualTo("Steven");
        assertThat(player.getLastName()).isEqualTo("Stamkos");
    }

    @Test
    public void testPlayerIsNotFound() {
        JsonRpcClient client = initClient("player_is_not_found");
        Player player = client.onDemand(TeamService.class, new FixedIntegerIdGenerator(4111)).findByInitials("Vladimir", "Sobotka");
        assertThat(player).isNull();
    }

    @Test
    public void testFindArray() {
        JsonRpcClient client = initClient("find_player_array");
        Player player = client.onDemand(TeamService.class, ParamsType.ARRAY, new FixedStringIdGenerator("dsfs1214"))
                .findByInitials("Ben", "Bishop");
        assertThat(player).isNotNull();
        assertThat(player.getFirstName()).isEqualTo("Ben");
        assertThat(player.getLastName()).isEqualTo("Bishop");
    }

    @Test
    public void testReturnList() {
        JsonRpcClient client = initClient("findByBirthYear");
        List<Player> players = client.onDemand(TeamService.class, new FixedIntegerIdGenerator(5621)).findByBirthYear(1990);
        assertThat(players).isNotNull();
        assertThat(players).hasSize(3);
        assertThat(players.get(0).getLastName()).isEqualTo("Allen");
        assertThat(players.get(1).getLastName()).isEqualTo("Stamkos");
        assertThat(players.get(2).getLastName()).isEqualTo("Hedman");
    }

    @Test
    public void testNoParams() {
        JsonRpcClient client = initClient("getPlayers");
        List<Player> players = client.onDemand(TeamService.class, new FixedIntegerIdGenerator(1000)).getPlayers();
        assertThat(players).isNotNull();
        assertThat(players).hasSize(3);
        assertThat(players.get(0).getLastName()).isEqualTo("Bishop");
        assertThat(players.get(1).getLastName()).isEqualTo("Tarasenko");
        assertThat(players.get(2).getLastName()).isEqualTo("Bouwmeester");
    }

    @Test
    public void testMap() {
        Map<String, Integer> contractLengths = new LinkedHashMap<String, Integer>() {{
            put("Backes", 4);
            put("Tarasenko", 3);
            put("Allen", 2);
            put("Bouwmeester", 5);
            put("Stamkos", 8);
            put("Callahan", 3);
            put("Bishop", 4);
            put("Hedman", 2);
        }};
        JsonRpcClient client = initClient("getContractSums");
        Map<String, Double> contractSums = client.onDemand(TeamService.class, new FixedIntegerIdGenerator(97555))
                .getContractSums(contractLengths);
        assertThat(contractSums).isExactlyInstanceOf(LinkedHashMap.class);
        assertThat(contractSums).isEqualTo(new HashMap<String, Double>() {{
            put("Backes", 18.0);
            put("Tarasenko", 2.7);
            put("Allen", 1.0);
            put("Bouwmeester", 27.0);
            put("Stamkos", 60.0);
            put("Callahan", 17.4);
            put("Bishop", 9.2);
            put("Hedman", 8.0);
        }});
    }

    @Test
    public void testOptional() {
        JsonRpcClient client = initClient("player_is_not_found");
        Optional<Player> optionalPlayer = client.onDemand(TeamService.class, new FixedIntegerIdGenerator(4111))
                .optionalFindByInitials("Vladimir", "Sobotka");
        assertThat(optionalPlayer.isPresent()).isFalse();
    }

    @Test
    public void testJsonRpcError() {
        JsonRpcClient client = initClient("methodNotFound");
        try {
            client.onDemand(TeamService.class, new FixedIntegerIdGenerator(1001)).getPlayer();
            Assert.fail();
        } catch (JsonRpcException e) {
            e.printStackTrace();
            ErrorMessage errorMessage = e.getErrorMessage();
            assertThat(errorMessage.getCode()).isEqualTo(-32601);
            assertThat(errorMessage.getMessage()).isEqualTo("Method not found");
        }
    }

}