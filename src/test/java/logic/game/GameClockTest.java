package logic.game;

import com.cheddar.robinhood.data.MarketState;
import logic.MarketStateTestUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import stockstream.cache.BrokerCache;
import stockstream.computer.TimeComputer;
import stockstream.logic.Scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class GameClockTest {
    @Mock
    private GameEngine gameEngine;

    @Mock
    private BrokerCache brokerCache;

    @Mock
    private TimeComputer timeComputer;

    @Mock
    private Scheduler scheduler;

    @InjectMocks
    private GameClock gameClock;

    @Before
    public void setupTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSetNextEvent_marketOpen_expectNextEventAsGameTick() {
        final DateTime now = new DateTime();

        when(brokerCache.getMarketState(now)).thenReturn(MarketStateTestUtils.createMarketState(now, true));

        final GameEvent nextEvent = gameClock.findNextEvent(now);

        assertEquals(GameEvent.Type.GAME_TICK, nextEvent.getEventType());
    }

    @Test
    public void testSetNextEvent_marketClosed_expectNextEventNextMarketOpen() {
        final DateTime today = new DateTime();
        final DateTime tomorrow = today.plusDays(1);

        final MarketState marketStateToday = MarketStateTestUtils.createMarketState(today, false);
        when(brokerCache.getMarketState(any())).thenReturn(marketStateToday);

        final MarketState marketStateTomorrow = MarketStateTestUtils.createMarketState(tomorrow, true);
        when(timeComputer.findNextBusinessDay(any())).thenReturn(marketStateTomorrow);

        final GameEvent nextEvent = gameClock.findNextEvent(today);

        assertEquals(GameEvent.Type.MARKET_OPEN, nextEvent.getEventType());
        assertTrue(marketStateTomorrow.getExtendedOpenTime().isPresent());
        assertEquals(marketStateTomorrow.getExtendedOpenTime().get().getMillis(), nextEvent.getNextEvent());
    }

}
