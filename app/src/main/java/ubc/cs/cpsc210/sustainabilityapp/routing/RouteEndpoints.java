package ubc.cs.cpsc210.sustainabilityapp.routing;

import ubc.cs.cpsc210.sustainabilityapp.model.LatLong;

/**
 * Defines the start point and end point of a route.
 */
public class RouteEndpoints {
	private LatLong start;
	private LatLong end;
	
	public RouteEndpoints(LatLong start, LatLong end) {
		this.start = start;
		this.end = end;
	}
	
	/**
	 * We override hashCode() and equals(), so that instances of this class can be used as a key in a
	 * HashMap.
	 * 
	 * Code for this method was generated using Eclipse: Source > Generate hashCode() and equals().
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}
	
	/**
	 * We override hashCode() and equals(), so that instances of this class can be used as a key in a
	 * HashMap.
	 * 
	 * Code for this method was generated using Eclipse: Source > Generate hashCode() and equals().
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RouteEndpoints other = (RouteEndpoints) obj;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}

	public LatLong getStart() {
		return start;
	}

	public LatLong getEnd() {
		return end;
	}
	
	
}
