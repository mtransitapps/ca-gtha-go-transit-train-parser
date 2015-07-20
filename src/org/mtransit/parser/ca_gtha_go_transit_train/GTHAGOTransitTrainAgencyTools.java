package org.mtransit.parser.ca_gtha_go_transit_train;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

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
		System.out.printf("\nGenerating GO Transit train data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating GO Transit train data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
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

	private static final long LW_RID = 1l; // Lakeshore West
	private static final long MI_RID = 2l; // Milton
	private static final long GT_RID = 3l; // Kitchener
	private static final long BR_RID = 5l; // Barrie
	private static final long RH_RID = 6l; // Richmond Hill
	private static final long ST_RID = 7l; // Stouffville
	private static final long LE_RID = 9l; // Lakeshore East

	@Override
	public long getRouteId(GRoute gRoute) {
		if (ST_RSN.equals(gRoute.route_short_name)) {
			return ST_RID;
		} else if (RH_RSN.equals(gRoute.route_short_name)) {
			return RH_RID;
		} else if (MI_RSN.equals(gRoute.route_short_name)) {
			return MI_RID;
		} else if (LW_RSN.equals(gRoute.route_short_name)) {
			return LW_RID;
		} else if (LE_RSN.equals(gRoute.route_short_name)) {
			return LE_RID;
		} else if (GT_RSN.equals(gRoute.route_short_name)) {
			return GT_RID;
		} else if (BR_RSN.equals(gRoute.route_short_name)) {
			return BR_RID;
		} else {
			System.out.println("Unexpected route ID " + gRoute);
			System.exit(-1);
			return -1l;
		}
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.route_long_name;
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String ST_RSN = "ST"; // Stouffville
	private static final String RH_RSN = "RH"; // Richmond Hill
	private static final String MI_RSN = "MI"; // Milton
	private static final String LW_RSN = "LW"; // Lakeshore West
	private static final String LE_RSN = "LE"; // Lakeshore East
	private static final String GT_RSN = "GT"; // Kitchener
	private static final String BR_RSN = "BR"; // Barrie

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
		if (StringUtils.isEmpty(gRoute.route_color)) {
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
			case 8: return COLOR_BC6277; // Niagara Falls
			case 9: return COLOR_EE3124; // Lakeshore East
			// @formatter:on
			default:
				System.out.println("getRouteColor() > Unexpected route ID color '" + routeId + "' (" + gRoute + ")");
				System.exit(-1);
				return null;
			}
		}
		return super.getRouteColor(gRoute);
	}

	@Override
	public int compare(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (routeId == LW_RID) {
			if (ts1.getTripId() == 101) {
				if (SID_EX.equals(ts1GStop.stop_id) && SID_UN.equals(ts2GStop.stop_id)) {
					return +1;
				}
			}
		}
		System.out.printf("\n%s: Unexpected compare early route!\n", routeId);
		System.exit(-1);
		return -1;
	}

	private static final String STOUFFVILLE = "Stouffville";
	private static final String BARRIE = "Barrie";
	private static final String KITCHENER = "Kitchener";
	private static final String UNION = "Union";
	private static final String EAST = "East";
	private static final String WEST = "West";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (mRoute.id == LW_RID) { // Lakeshore West
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(UNION, gTrip.direction_id);
				return;
			} else if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(WEST, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == GT_RID) { // Kitchener
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(KITCHENER, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == BR_RID) { // Barrie
			if (gTrip.direction_id == 1) {
				mTrip.setHeadsignString(BARRIE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == ST_RID) { // Stouffville
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(STOUFFVILLE, gTrip.direction_id);
				return;
			}
		} else if (mRoute.id == LE_RID) { // Lakeshore East
			if (gTrip.direction_id == 0) {
				mTrip.setHeadsignString(EAST, gTrip.direction_id);
				return;
			}
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.trip_headsign), gTrip.direction_id);
	}

	private static final Pattern START_WITH_RSN = Pattern.compile("(^[A-Z]{2}\\-)", Pattern.CASE_INSENSITIVE);

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = START_WITH_RSN.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = GO.matcher(tripHeadsign).replaceAll(GO_REPLACEMENT);
		tripHeadsign = STATION.matcher(tripHeadsign).replaceAll(STATION_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
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
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	private static final String SID_UN = "UN";
	private static final int UN_SID = 9021;
	private static final String SID_EX = "EX";
	private static final int EX_SID = 9022;
	private static final String SID_MI = "MI";
	private static final int MI_SID = 9031;
	private static final String SID_LO = "LO";
	private static final int LO_SID = 9033;
	private static final String SID_DA = "DA";
	private static final int DA_SID = 9061;
	private static final String SID_SC = "SC";
	private static final int SC_SID = 9062;
	private static final String SID_EG = "EG";
	private static final int EG_SID = 9063;
	private static final String SID_GU = "GU";
	private static final int GU_SID = 9081;
	private static final String SID_RO = "RO";
	private static final int RO_SID = 9091;
	private static final String SID_PO = "PO";
	private static final int PO_SID = 9111;
	private static final String SID_CL = "CL";
	private static final int CL_SID = 9121;
	private static final String SID_OA = "OA";
	private static final int OA_SID = 9131;
	private static final String SID_BO = "BO";
	private static final int BO_SID = 9141;
	private static final String SID_AP = "AP";
	private static final int AP_SID = 9151;
	private static final String SID_BU = "BU";
	private static final int BU_SID = 9161;
	private static final String SID_AL = "AL";
	private static final int AL_SID = 9171;
	private static final String SID_PIN = "PIN";
	private static final int PIN_SID = 9911;
	private static final String SID_AJ = "AJ";
	private static final int AJ_SID = 9921;
	private static final String SID_WH = "WH";
	private static final int WH_SID = 9939;
	private static final String SID_OS = "OS";
	private static final int OS_SID = 9941;
	private static final String SID_BL = "BL";
	private static final int BL_SID = 9023;
	private static final String SID_KP = "KP";
	private static final int KP_SID = 9032;
	private static final String SID_WE = "WE";
	private static final int WE_SID = 9041;
	private static final String SID_ET = "ET";
	private static final int ET_SID = 9042;
	private static final String SID_OR = "OR";
	private static final int OR_SID = 9051;
	private static final String SID_OL = "OL";
	private static final int OL_SID = 9052;
	private static final String SID_AG = "AG";
	private static final int AG_SID = 9071;
	private static final String SID_DI = "DI";
	private static final int DI_SID = 9113;
	private static final String SID_CO = "CO";
	private static final int CO_SID = 9114;
	private static final String SID_ER = "ER";
	private static final int ER_SID = 9123;
	private static final String SID_HA = "HA";
	private static final int HA_SID = 9181;
	private static final String SID_YO = "YO";
	private static final int YO_SID = 9191;
	private static final String SID_SR = "SR";
	private static final int SR_SID = 9211;
	private static final String SID_ME = "ME";
	private static final int ME_SID = 9221;
	private static final String SID_LS = "LS";
	private static final int LS_SID = 9231;
	private static final String SID_ML = "ML";
	private static final int ML_SID = 9241;
	private static final String SID_KI = "KI";
	private static final int KI_SID = 9271;
	private static final String SID_MA = "MA";
	private static final int MA_SID = 9311;
	private static final String SID_BE = "BE";
	private static final int BE_SID = 9321;
	private static final String SID_BR = "BR";
	private static final int BR_SID = 9331;
	private static final String SID_MO = "MO";
	private static final int MO_SID = 9341;
	private static final String SID_GE = "GE";
	private static final int GE_SID = 9351;
	private static final String SID_AC = "AC";
	private static final int AC_SID = 9371;
	private static final String SID_GL = "GL";
	private static final int GL_SID = 9391;
	private static final String SID_EA = "EA";
	private static final int EA_SID = 9441;
	private static final String SID_LA = "LA";
	private static final int LA_SID = 9601;
	private static final String SID_RI = "RI";
	private static final int RI_SID = 9612;
	private static final String SID_MP = "MP";
	private static final int MP_SID = 9613;
	private static final String SID_RU = "RU";
	private static final int RU_SID = 9614;
	private static final String SID_KC = "KC";
	private static final int KC_SID = 9621;
	private static final String SID_AU = "AU";
	private static final int AU_SID = 9631;
	private static final String SID_NE = "NE";
	private static final int NE_SID = 9641;
	private static final String SID_BD = "BD";
	private static final int BD_SID = 9651;
	private static final String SID_BA = "BA";
	private static final int BA_SID = 9681;
	private static final String SID_AD = "AD";
	private static final int AD_SID = 9691;
	private static final String SID_MK = "MK";
	private static final int MK_SID = 9701;
	private static final String SID_UI = "UI";
	private static final int UI_SID = 9712;
	private static final String SID_MR = "MR";
	private static final int MR_SID = 9721;
	private static final String SID_CE = "CE";
	private static final int CE_SID = 9722;
	private static final String SID_MJ = "MJ";
	private static final int MJ_SID = 9731;
	private static final String SID_ST = "ST";
	private static final int ST_SID = 9741;
	private static final String SID_LI = "LI";
	private static final int LI_SID = 9742;
	private static final String SID_KE = "KE";
	private static final int KE_SID = 9771;
	private static final String SID_WR = "WR";
	private static final int WR_SID = 100001;
	private static final String SID_USBT = "USBT";
	private static final int USBT_SID = 100002;
	private static final String SID_NI = "NI";
	private static final int NI_SID = 100003;
	private static final String SID_PA = "PA";
	private static final int PA_SID = 100004;
	private static final String SID_SCTH = "SCTH";
	private static final int SCTH_SID = 100005;

	@Override
	public int getStopId(GStop gStop) {
		if (!Utils.isDigitsOnly(gStop.stop_id)) {
			if (SID_UN.equals(gStop.stop_id)) {
				return UN_SID;
			} else if (SID_EX.equals(gStop.stop_id)) {
				return EX_SID;
			} else if (SID_MI.equals(gStop.stop_id)) {
				return MI_SID;
			} else if (SID_LO.equals(gStop.stop_id)) {
				return LO_SID;
			} else if (SID_DA.equals(gStop.stop_id)) {
				return DA_SID;
			} else if (SID_SC.equals(gStop.stop_id)) {
				return SC_SID;
			} else if (SID_EG.equals(gStop.stop_id)) {
				return EG_SID;
			} else if (SID_GU.equals(gStop.stop_id)) {
				return GU_SID;
			} else if (SID_RO.equals(gStop.stop_id)) {
				return RO_SID;
			} else if (SID_PO.equals(gStop.stop_id)) {
				return PO_SID;
			} else if (SID_CL.equals(gStop.stop_id)) {
				return CL_SID;
			} else if (SID_OA.equals(gStop.stop_id)) {
				return OA_SID;
			} else if (SID_BO.equals(gStop.stop_id)) {
				return BO_SID;
			} else if (SID_AP.equals(gStop.stop_id)) {
				return AP_SID;
			} else if (SID_BU.equals(gStop.stop_id)) {
				return BU_SID;
			} else if (SID_AL.equals(gStop.stop_id)) {
				return AL_SID;
			} else if (SID_PIN.equals(gStop.stop_id)) {
				return PIN_SID;
			} else if (SID_AJ.equals(gStop.stop_id)) {
				return AJ_SID;
			} else if (SID_WH.equals(gStop.stop_id)) {
				return WH_SID;
			} else if (SID_OS.equals(gStop.stop_id)) {
				return OS_SID;
			} else if (SID_BL.equals(gStop.stop_id)) {
				return BL_SID;
			} else if (SID_KP.equals(gStop.stop_id)) {
				return KP_SID;
			} else if (SID_WE.equals(gStop.stop_id)) {
				return WE_SID;
			} else if (SID_ET.equals(gStop.stop_id)) {
				return ET_SID;
			} else if (SID_OR.equals(gStop.stop_id)) {
				return OR_SID;
			} else if (SID_OL.equals(gStop.stop_id)) {
				return OL_SID;
			} else if (SID_AG.equals(gStop.stop_id)) {
				return AG_SID;
			} else if (SID_DI.equals(gStop.stop_id)) {
				return DI_SID;
			} else if (SID_CO.equals(gStop.stop_id)) {
				return CO_SID;
			} else if (SID_ER.equals(gStop.stop_id)) {
				return ER_SID;
			} else if (SID_HA.equals(gStop.stop_id)) {
				return HA_SID;
			} else if (SID_YO.equals(gStop.stop_id)) {
				return YO_SID;
			} else if (SID_SR.equals(gStop.stop_id)) {
				return SR_SID;
			} else if (SID_ME.equals(gStop.stop_id)) {
				return ME_SID;
			} else if (SID_LS.equals(gStop.stop_id)) {
				return LS_SID;
			} else if (SID_ML.equals(gStop.stop_id)) {
				return ML_SID;
			} else if (SID_KI.equals(gStop.stop_id)) {
				return KI_SID;
			} else if (SID_MA.equals(gStop.stop_id)) {
				return MA_SID;
			} else if (SID_BE.equals(gStop.stop_id)) {
				return BE_SID;
			} else if (SID_BR.equals(gStop.stop_id)) {
				return BR_SID;
			} else if (SID_MO.equals(gStop.stop_id)) {
				return MO_SID;
			} else if (SID_GE.equals(gStop.stop_id)) {
				return GE_SID;
			} else if (SID_AC.equals(gStop.stop_id)) {
				return AC_SID;
			} else if (SID_GL.equals(gStop.stop_id)) {
				return GL_SID;
			} else if (SID_EA.equals(gStop.stop_id)) {
				return EA_SID;
			} else if (SID_LA.equals(gStop.stop_id)) {
				return LA_SID;
			} else if (SID_RI.equals(gStop.stop_id)) {
				return RI_SID;
			} else if (SID_MP.equals(gStop.stop_id)) {
				return MP_SID;
			} else if (SID_RU.equals(gStop.stop_id)) {
				return RU_SID;
			} else if (SID_KC.equals(gStop.stop_id)) {
				return KC_SID;
			} else if (SID_AU.equals(gStop.stop_id)) {
				return AU_SID;
			} else if (SID_NE.equals(gStop.stop_id)) {
				return NE_SID;
			} else if (SID_BD.equals(gStop.stop_id)) {
				return BD_SID;
			} else if (SID_BA.equals(gStop.stop_id)) {
				return BA_SID;
			} else if (SID_AD.equals(gStop.stop_id)) {
				return AD_SID;
			} else if (SID_MK.equals(gStop.stop_id)) {
				return MK_SID;
			} else if (SID_UI.equals(gStop.stop_id)) {
				return UI_SID;
			} else if (SID_MR.equals(gStop.stop_id)) {
				return MR_SID;
			} else if (SID_CE.equals(gStop.stop_id)) {
				return CE_SID;
			} else if (SID_MJ.equals(gStop.stop_id)) {
				return MJ_SID;
			} else if (SID_ST.equals(gStop.stop_id)) {
				return ST_SID;
			} else if (SID_LI.equals(gStop.stop_id)) {
				return LI_SID;
			} else if (SID_KE.equals(gStop.stop_id)) {
				return KE_SID;
			} else if (SID_WR.equals(gStop.stop_id)) {
				return WR_SID;
			} else if (SID_USBT.equals(gStop.stop_id)) {
				return USBT_SID;
			} else if (SID_NI.equals(gStop.stop_id)) {
				return NI_SID;
			} else if (SID_PA.equals(gStop.stop_id)) {
				return PA_SID;
			} else if (SID_SCTH.equals(gStop.stop_id)) {
				return SCTH_SID;
			} else {
				System.out.printf("\nUnexpected stop ID %s.\n", gStop);
				System.exit(-1);
				return -1;
			}
		}
		return super.getStopId(gStop);
	}
}
