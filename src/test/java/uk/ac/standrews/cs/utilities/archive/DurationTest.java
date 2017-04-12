package uk.ac.standrews.cs.utilities.archive;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class DurationTest {

    @Test
    public void durationOrdering() {

        final Duration d1 = new Duration(1, TimeUnit.MILLISECONDS);
        final Duration d2 = new Duration(10, TimeUnit.MILLISECONDS);
        final Duration d3 = new Duration(1, TimeUnit.SECONDS);

        assertThat(d1.compareTo(d1), is(equalTo(0)));
        assertThat(d1.compareTo(d2), is(equalTo(-1)));
        assertThat(d1.compareTo(d3), is(equalTo(-1)));

        assertThat(d2.compareTo(d1), is(equalTo(1)));
        assertThat(d2.compareTo(d2), is(equalTo(0)));
        assertThat(d2.compareTo(d3), is(equalTo(-1)));

        assertThat(d3.compareTo(d1), is(equalTo(1)));
        assertThat(d3.compareTo(d2), is(equalTo(1)));
        assertThat(d3.compareTo(d3), is(equalTo(0)));
    }

    @Test
    public void conversion() {

        final Duration d = new Duration(1, TimeUnit.HOURS);

        assertThat(d.convertTo(TimeUnit.SECONDS).getLength(), is(equalTo(3600L)));
    }

    @Test
    public void toStringAsLargestTimeUnit() {

        final Duration one_ns = new Duration(1, TimeUnit.NANOSECONDS);
        final Duration one_ms_in_ns = new Duration(1000000, TimeUnit.NANOSECONDS);
        final Duration one_s_in_micros = new Duration(1000000, TimeUnit.MICROSECONDS);
        final Duration sixteen_mins_ish_in_millis = new Duration(1000000, TimeUnit.MILLISECONDS);
        final Duration eleven_days_ish_in_seconds = new Duration(1000000, TimeUnit.SECONDS);
        final Duration one_day = new Duration(1, TimeUnit.DAYS);

        assertThat(one_ns.toStringAsLargestTimeUnit(), is(equalTo("1 ns")));
        assertThat(one_ms_in_ns.toStringAsLargestTimeUnit(), is(equalTo("1 ms")));
        assertThat(one_s_in_micros.toStringAsLargestTimeUnit(), is(equalTo("1 s")));
        assertThat(sixteen_mins_ish_in_millis.toStringAsLargestTimeUnit(), is(equalTo("16 min")));
        assertThat(eleven_days_ish_in_seconds.toStringAsLargestTimeUnit(), is(equalTo("11 days")));
        assertThat(one_day.toStringAsLargestTimeUnit(), is(equalTo("1 days")));
    }
}
