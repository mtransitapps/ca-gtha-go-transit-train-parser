package org.mtransit.parser.ca_gtha_go_transit_train;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MSpec;
import org.mtransit.parser.mt.data.MTrip;

// http://www.gotransit.com/publicroot/en/schedules/DeveloperResources.aspx
// http://www.gotransit.com/timetables/fr/schedules/DeveloperResources.aspx
// http://www.gotransit.com/publicroot/en/schedules/GTFSdownload.aspx
// http://www.gotransit.com/publicroot/gtfs/google_transit.zip
public class GTHAGOTransitTrainAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-gtha-go-transit-train-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new GTHAGOTransitTrainAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("Generating GO Transit train data...\n");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("Generating GO Transit train data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_TRAIN;
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");


	@Override
	public long getRouteId(GRoute gRoute) {
		Matcher matcher = DIGITS.matcher(gRoute.route_id);
		matcher.find();
		return Long.parseLong(matcher.group());
	}

	private static final Pattern TRAIN = Pattern.compile("(^|\\s){1}(train)($|\\s){1}", Pattern.CASE_INSENSITIVE);
	private static final String TRAIN_REPLACEMENT = " ";

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.route_long_name;
		routeLongName = TRAIN.matcher(routeLongName).replaceAll(TRAIN_REPLACEMENT);
		return MSpec.cleanLabel(routeLongName);
	}

	private static final String GO_RTS = "GO";

	@Override
	public String getRouteShortName(GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.route_short_name)) {
			return GO_RTS;
		}
		return super.getRouteShortName(gRoute);
	}

	private static final String AGENCY_COLOR = "387C2B"; // GREEN (AGENCY WEB SITE CSS)

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String COLOR_BC6277 = "BC6277";
	private static final String COLOR_F46F1A = "F46F1A";
	private static final String COLOR_098137 = "098137";
	private static final String COLOR_0B335E = "0B335E";
	private static final String COLOR_0098C9 = "0098C9";
	private static final String COLOR_713907 = "713907";
	private static final String COLOR_96092B = "96092B";
	private static final String COLOR_EE3124 = "EE3124";

	@Override
	public String getRouteColor(GRoute gRoute) {
		Matcher matcher = DIGITS.matcher(gRoute.route_id);
		matcher.find();
		int routeId = Integer.parseInt(matcher.group());
		switch (routeId) {
		// @formatter:off
		case 1: return COLOR_96092B; // Lakeshore West
		case 2: return COLOR_F46F1A; // Milton
		case 3: return COLOR_098137; // Kitchener
		case 5: return COLOR_0B335E; // Barrie
		case 6: return COLOR_0098C9; // Richmond Hill
		case 7: return COLOR_713907; // Stouffville
//		case 8: return COLOR_96092B; // Niagara Falls
		case 8: return COLOR_BC6277; // Niagara Falls
		case 9: return COLOR_EE3124; // Lakeshore East
		// @formatter:on
		default:
			System.out.println("getRouteColor() > Unexpected route ID color '" + routeId + "' (" + gRoute + ")");
			System.exit(-1);
			return null;
		}
	}

	private static final String STOUFFVILLE = "Stouffville";
	private static final String NIAGARA_FALLS = "Niagara Falls";
	private static final String RICHMOND_HILL = "Richmond Hill";
	private static final String BARRIE = "Barrie";
	private static final String KITCHENER = "Kitchener";
	private static final String MILTON = "Milton";
	private static final String UNION = "Union";
	private static final String EAST = "East";
	private static final String WEST = "West";

	@Override
	public void setTripHeadsign(MRoute route, MTrip mTrip, GTrip gTrip) {
		if (route.id == 1l) { // Lakeshore West
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(UNION, 0);
			} else {
				mTrip.setHeadsignString(WEST, 1);
			}
			return;
		} else if (route.id == 2l) { // Milton
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(UNION, 0);
			} else {
				mTrip.setHeadsignString(MILTON, 1);
			}
			return;
		} else if (route.id == 3l) { // Kitchener
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(UNION, 0);
			} else {
				mTrip.setHeadsignString(KITCHENER, 1);
			}
			return;
		} else if (route.id == 5l) { // Barrie
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(BARRIE, 0);
			} else {
				mTrip.setHeadsignString(UNION, 1);
			}
			return;
		} else if (route.id == 6l) { // Richmond Hill
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(RICHMOND_HILL, 0);
			} else {
				mTrip.setHeadsignString(UNION, 1);
			}
			return;
		} else if (route.id == 7l) { // Stouffville
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(STOUFFVILLE, 0);
			} else {
				mTrip.setHeadsignString(UNION, 1);
			}
			return;
		} else if (route.id == 8l) { // Niagara Falls
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(UNION, 0);
			} else {
				mTrip.setHeadsignString(NIAGARA_FALLS, 1);
			}
			return;
		} else if (route.id == 9l) { // Lakeshore East
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(UNION, 0);
			} else {
				mTrip.setHeadsignString(EAST, 1);
			}
			return;
		}
		System.out.println("Unexpected trip " + gTrip);
		System.exit(-1);
	}

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		return MSpec.cleanLabel(tripHeadsign);
	}

	private static final Pattern AT = Pattern.compile("( at )", Pattern.CASE_INSENSITIVE);
	private static final String AT_REPLACEMENT = " / ";

	private static final Pattern GO = Pattern.compile("(^|\\s){1}(go)($|\\s){1}", Pattern.CASE_INSENSITIVE);
	private static final String GO_REPLACEMENT = " ";

	private static final Pattern VIA = Pattern.compile("(^|\\s){1}(via)($|\\s){1}", Pattern.CASE_INSENSITIVE);
	private static final String VIA_REPLACEMENT = " ";

	private static final Pattern RAIL = Pattern.compile("(^|\\s){1}(rail)($|\\s){1}", Pattern.CASE_INSENSITIVE);
	private static final String RAIL_REPLACEMENT = " ";

	private static final Pattern STATION = Pattern.compile("(^|\\s){1}(station)($|\\s){1}", Pattern.CASE_INSENSITIVE);
	private static final String STATION_REPLACEMENT = " ";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = AT.matcher(gStopName).replaceAll(AT_REPLACEMENT);
		gStopName = VIA.matcher(gStopName).replaceAll(VIA_REPLACEMENT);
		gStopName = GO.matcher(gStopName).replaceAll(GO_REPLACEMENT);
		gStopName = RAIL.matcher(gStopName).replaceAll(RAIL_REPLACEMENT);
		gStopName = STATION.matcher(gStopName).replaceAll(STATION_REPLACEMENT);
		gStopName = MSpec.cleanNumbers(gStopName);
		return MSpec.cleanLabel(gStopName);
	}
}
