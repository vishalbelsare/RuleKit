package adaa.analytics.rules.logic.actions;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import adaa.analytics.rules.logic.representation.Interval;

public class IntersectionFinderTest {

	@Test
	public void testCalculateAllIntersectionsOf_() {
		IntersectionFinder finder = new IntersectionFinder();
		
		List<Interval> intervals = new ArrayList<Interval>();
		intervals.add(new Interval(-1, 5, true, true));
		intervals.add(new Interval(3, 10, true, true));
		intervals.add(new Interval(7, 15, true, true));
		
		List<Interval> expected = new ArrayList<Interval>();
		expected.add(Interval.create_le(-1));
		expected.add(new Interval(-1, 3, true, true));
		expected.add(new Interval(3, 5, true, true));
		expected.add(new Interval(5, 7, true, true));
		expected.add(new Interval(7, 10, true, true));
		expected.add(new Interval(10, 15, true, true));
		expected.add(Interval.create_geq(15));
		
		List<Interval> actual = finder.calculateAllIntersectionsOf(intervals);
		
		Assert.assertArrayEquals(expected.toArray(), actual.toArray());
	}

}
