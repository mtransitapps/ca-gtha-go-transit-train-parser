package org.mtransit.parser.ca_gtha_go_transit_train;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.CleanUtils;
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
import org.mtransit.parser.mt.data.MTrip;

// https://www.gotransit.com/en/information-resources/software-developers
// https://www.gotransit.com/fr/ressources-informatives/dveloppeurs-de-logiciel
// https://www.gotransit.com/static_files/gotransit/assets/Files/GO_GTFS.zip
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
		this.serviceIds = extractUsefulServiceIds(args, this, true);
		super.start(args);
		System.out.printf("\nGenerating GO Transit train data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
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

	private static final long LW_RID = 1L; // Lakeshore West
	private static final long MI_RID = 2L; // Milton
	private static final long KI_RID = 3L; // Kitchener
	private static final long BR_RID = 5L; // Barrie
	private static final long RH_RID = 6L; // Richmond Hill
	private static final long ST_RID = 7L; // Stouffville
	private static final long LE_RID = 9L; // Lakeshore East

	@Override
	public long getRouteId(GRoute gRoute) {
		if (ST_RSN.equals(gRoute.getRouteShortName())) {
			return ST_RID;
		} else if (RH_RSN.equals(gRoute.getRouteShortName())) {
			return RH_RID;
		} else if (MI_RSN.equals(gRoute.getRouteShortName())) {
			return MI_RID;
		} else if (LW_RSN.equals(gRoute.getRouteShortName())) {
			return LW_RID;
		} else if (LE_RSN.equals(gRoute.getRouteShortName())) {
			return LE_RID;
		} else if (KI_RSN.equals(gRoute.getRouteShortName()) //
				|| GT_RSN.equals(gRoute.getRouteShortName())) {
			return KI_RID;
		} else if (BR_RSN.equals(gRoute.getRouteShortName())) {
			return BR_RID;
		}
		if (gRoute.getRouteId().endsWith(ST_RSN)) {
			return ST_RID;
		} else if (gRoute.getRouteId().endsWith(RH_RSN)) {
			return RH_RID;
		} else if (gRoute.getRouteId().endsWith(MI_RSN)) {
			return MI_RID;
		} else if (gRoute.getRouteId().endsWith(LW_RSN)) {
			return LW_RID;
		} else if (gRoute.getRouteId().endsWith(LE_RSN)) {
			return LE_RID;
		} else if (gRoute.getRouteId().endsWith(KI_RSN) //
				|| gRoute.getRouteId().endsWith(GT_RSN)) {
			return KI_RID;
		} else if (gRoute.getRouteId().endsWith(BR_RSN)) {
			return BR_RID;
		}
		System.out.printf("\nUnexpected route ID for %s!\n", gRoute);
		System.exit(-1);
		return -1l;
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongName();
		routeLongName = CleanUtils.cleanStreetTypes(routeLongName);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final String ST_RSN = "ST"; // Stouffville
	private static final String RH_RSN = "RH"; // Richmond Hill
	private static final String MI_RSN = "MI"; // Milton
	private static final String LW_RSN = "LW"; // Lakeshore West
	private static final String LE_RSN = "LE"; // Lakeshore East
	private static final String KI_RSN = "KI"; // Kitchener
	private static final String GT_RSN = "GT"; // Kitchener
	private static final String BR_RSN = "BR"; // Barrie

	@Override
	public String getRouteShortName(GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteShortName())) {
			if (gRoute.getRouteId().endsWith(ST_RSN)) {
				return ST_RSN;
			} else if (gRoute.getRouteId().endsWith(RH_RSN)) {
				return RH_RSN;
			} else if (gRoute.getRouteId().endsWith(MI_RSN)) {
				return MI_RSN;
			} else if (gRoute.getRouteId().endsWith(LW_RSN)) {
				return LW_RSN;
			} else if (gRoute.getRouteId().endsWith(LE_RSN)) {
				return LE_RSN;
			} else if (gRoute.getRouteId().endsWith(KI_RSN) //
					|| gRoute.getRouteId().endsWith(GT_RSN)) {
				return GT_RSN;
			} else if (gRoute.getRouteId().endsWith(BR_RSN)) {
				return BR_RSN;
			}
			System.out.printf("\nUnexpected route short name for %s!\n", gRoute);
			System.exit(-1);
			return null;
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
	private static final String COLOR_794500 = "794500";
	private static final String COLOR_96092B = "96092B";
	private static final String COLOR_EE3124 = "EE3124";

	@Override
	public String getRouteColor(GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteColor())) {
			int routeId = (int) getRouteId(gRoute);
			switch (routeId) {
			// @formatter:off
			case 1: return COLOR_96092B; // Lakeshore West
			case 2: return COLOR_F46F1A; // Milton
			case 3: return COLOR_098137; // Kitchener
			case 5: return COLOR_0B335E; // Barrie
			case 6: return COLOR_0098C9; // Richmond Hill
			case 7: return COLOR_794500; // Stouffville
			case 8: return COLOR_BC6277; // Niagara Falls
			case 9: return COLOR_EE3124; // Lakeshore East
			// @formatter:on
			}
			if (isGoodEnoughAccepted()) {
				return null;
			}
			System.out.printf("Unexpected route color '%s'!\n", gRoute);
			System.exit(-1);
			return null;
		}
		return super.getRouteColor(gRoute);
	}

	private static final String ALDERSHOT = "Aldershot";
	private static final String ALLANDALE_WATERFRONT = "Allandale Waterfront";
	private static final String APPLEBY = "Appleby";
	private static final String AURORA = "Aurora";
	private static final String BRADFORD = "Bradford";
	private static final String BURLINGTON = "Burlington";
	private static final String EXHIBITION = "Exhibition";
	private static final String GEORGETOWN = "Georgetown";
	private static final String GORMLEY = "Gormley";
	private static final String HAMILTON = "Hamilton";
	private static final String LINCOLNVILLE = "Lincolnville";
	private static final String MIMICO = "Mimico";
	private static final String MOUNT_PLEASANT = "Mt Pleasant";
	private static final String NIAGARA_FALLS = "Niagara Falls";
	private static final String OAKVILLE = "Oakville";
	private static final String OSHAWA = "Oshawa";
	private static final String PICKERING = "Pickering";
	private static final String RICHMOND_HILL = "Richmond Hl";
	private static final String KITCHENER = "Kitchener";
	private static final String UNION = "Union";
	private static final String UNIONVILLE = "Unionville";
	private static final String WEST_HARBOUR = "West Harbour";
	private static final String WHITBY = "Whitby";

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), gTrip.getDirectionId());
	}

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == LW_RID) { // Lakeshore West
			if (Arrays.asList( //
					"1HA", //
					"1WR", //
					"1BU", //
					EXHIBITION, //
					MIMICO, //
					OAKVILLE, //
					APPLEBY, //
					BURLINGTON, //
					ALDERSHOT, //
					WEST_HARBOUR, //
					HAMILTON //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(HAMILTON, mTrip.getHeadsignId()); // NIAGARA_FALLS // WEST
				return true;
			} else if (Arrays.asList( //
					"1HA", //
					"1WR", //
					"1BU", //
					EXHIBITION, //
					MIMICO, //
					OAKVILLE, //
					APPLEBY, //
					BURLINGTON, //
					ALDERSHOT, //
					WEST_HARBOUR, //
					HAMILTON, //
					NIAGARA_FALLS //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(NIAGARA_FALLS, mTrip.getHeadsignId()); // WEST
				return true;
			}
		} else if (mTrip.getRouteId() == KI_RID) { // Kitchener
			if (Arrays.asList( //
					"1GE", //
					GEORGETOWN, //
					KITCHENER, //
					MOUNT_PLEASANT //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(KITCHENER, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == BR_RID) { // Barrie
			if (Arrays.asList( //
					"1AU", //
					ALLANDALE_WATERFRONT, //
					AURORA, //
					BRADFORD //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(ALLANDALE_WATERFRONT, mTrip.getHeadsignId()); // Barrie
				return true;
			}
		} else if (mTrip.getRouteId() == RH_RID) { // Richmond Hill
			if (Arrays.asList( //
					GORMLEY, //
					RICHMOND_HILL //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(GORMLEY, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == ST_RID) { // Stouffville
			if (Arrays.asList( //
					LINCOLNVILLE, //
					UNIONVILLE //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(LINCOLNVILLE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == LE_RID) { // Lakeshore East
			if (Arrays.asList( //
					"1OS", //
					OSHAWA, //
					WHITBY, //
					PICKERING //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(OSHAWA, mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					EXHIBITION, //
					UNION //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(UNION, mTrip.getHeadsignId());
				return true;
			}
		}
		System.out.printf("\nUnexpected trips to merge %s & %s!\n", mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	private static final Pattern START_WITH_RSN = Pattern.compile("(^[A-Z]{2}(\\s+)\\- )", Pattern.CASE_INSENSITIVE);

	private static final Pattern FIRST_STATION_TIME_LAST_STATION_TIME = Pattern.compile("" //
			+ "(" //
			+ "([\\w\\s]*)" //
			+ "[\\s]+" //
			+ "([\\d]{2}\\:[\\d]{2})" //
			+ "[\\s]+" //
			+ "\\-" //
			+ "[\\s]+" //
			+ "([\\w\\s]*)" //
			+ "[\\s]+" //
			+ "([\\d]{2}\\:[\\d]{2})" //
			+ ")", Pattern.CASE_INSENSITIVE);
	private static final String FIRST_STATION_TIME_LAST_STATION_TIME_REPLACEMENT = "$4";

	private static final Pattern CENTER = Pattern.compile("((^|\\W){1}(center|centre|ctr)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String CENTER_REPLACEMENT = " ";

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = START_WITH_RSN.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = FIRST_STATION_TIME_LAST_STATION_TIME.matcher(tripHeadsign).replaceAll(FIRST_STATION_TIME_LAST_STATION_TIME_REPLACEMENT);
		tripHeadsign = GO.matcher(tripHeadsign).replaceAll(GO_REPLACEMENT);
		tripHeadsign = CENTER.matcher(tripHeadsign).replaceAll(CENTER_REPLACEMENT);
		tripHeadsign = STATION.matcher(tripHeadsign).replaceAll(STATION_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

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
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
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
	private static final String SID_GO = "GO";
	private static final int GO_SID = 2629;
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
	private static final String SID_DW = "DW";
	private static final int DW_SID = 100006;

	@Override
	public int getStopId(GStop gStop) {
		if (!Utils.isDigitsOnly(gStop.getStopId())) {
			if (SID_UN.equals(gStop.getStopId())) {
				return UN_SID;
			} else if (SID_EX.equals(gStop.getStopId())) {
				return EX_SID;
			} else if (SID_MI.equals(gStop.getStopId())) {
				return MI_SID;
			} else if (SID_LO.equals(gStop.getStopId())) {
				return LO_SID;
			} else if (SID_DA.equals(gStop.getStopId())) {
				return DA_SID;
			} else if (SID_SC.equals(gStop.getStopId())) {
				return SC_SID;
			} else if (SID_EG.equals(gStop.getStopId())) {
				return EG_SID;
			} else if (SID_GU.equals(gStop.getStopId())) {
				return GU_SID;
			} else if (SID_RO.equals(gStop.getStopId())) {
				return RO_SID;
			} else if (SID_PO.equals(gStop.getStopId())) {
				return PO_SID;
			} else if (SID_CL.equals(gStop.getStopId())) {
				return CL_SID;
			} else if (SID_OA.equals(gStop.getStopId())) {
				return OA_SID;
			} else if (SID_BO.equals(gStop.getStopId())) {
				return BO_SID;
			} else if (SID_AP.equals(gStop.getStopId())) {
				return AP_SID;
			} else if (SID_BU.equals(gStop.getStopId())) {
				return BU_SID;
			} else if (SID_AL.equals(gStop.getStopId())) {
				return AL_SID;
			} else if (SID_PIN.equals(gStop.getStopId())) {
				return PIN_SID;
			} else if (SID_AJ.equals(gStop.getStopId())) {
				return AJ_SID;
			} else if (SID_WH.equals(gStop.getStopId())) {
				return WH_SID;
			} else if (SID_OS.equals(gStop.getStopId())) {
				return OS_SID;
			} else if (SID_BL.equals(gStop.getStopId())) {
				return BL_SID;
			} else if (SID_KP.equals(gStop.getStopId())) {
				return KP_SID;
			} else if (SID_WE.equals(gStop.getStopId())) {
				return WE_SID;
			} else if (SID_ET.equals(gStop.getStopId())) {
				return ET_SID;
			} else if (SID_OR.equals(gStop.getStopId())) {
				return OR_SID;
			} else if (SID_OL.equals(gStop.getStopId())) {
				return OL_SID;
			} else if (SID_AG.equals(gStop.getStopId())) {
				return AG_SID;
			} else if (SID_DI.equals(gStop.getStopId())) {
				return DI_SID;
			} else if (SID_CO.equals(gStop.getStopId())) {
				return CO_SID;
			} else if (SID_ER.equals(gStop.getStopId())) {
				return ER_SID;
			} else if (SID_HA.equals(gStop.getStopId())) {
				return HA_SID;
			} else if (SID_YO.equals(gStop.getStopId())) {
				return YO_SID;
			} else if (SID_SR.equals(gStop.getStopId())) {
				return SR_SID;
			} else if (SID_ME.equals(gStop.getStopId())) {
				return ME_SID;
			} else if (SID_LS.equals(gStop.getStopId())) {
				return LS_SID;
			} else if (SID_ML.equals(gStop.getStopId())) {
				return ML_SID;
			} else if (SID_KI.equals(gStop.getStopId())) {
				return KI_SID;
			} else if (SID_MA.equals(gStop.getStopId())) {
				return MA_SID;
			} else if (SID_BE.equals(gStop.getStopId())) {
				return BE_SID;
			} else if (SID_BR.equals(gStop.getStopId())) {
				return BR_SID;
			} else if (SID_MO.equals(gStop.getStopId())) {
				return MO_SID;
			} else if (SID_GE.equals(gStop.getStopId())) {
				return GE_SID;
			} else if (SID_GO.equals(gStop.getStopId())) {
				return GO_SID;
			} else if (SID_AC.equals(gStop.getStopId())) {
				return AC_SID;
			} else if (SID_GL.equals(gStop.getStopId())) {
				return GL_SID;
			} else if (SID_EA.equals(gStop.getStopId())) {
				return EA_SID;
			} else if (SID_LA.equals(gStop.getStopId())) {
				return LA_SID;
			} else if (SID_RI.equals(gStop.getStopId())) {
				return RI_SID;
			} else if (SID_MP.equals(gStop.getStopId())) {
				return MP_SID;
			} else if (SID_RU.equals(gStop.getStopId())) {
				return RU_SID;
			} else if (SID_KC.equals(gStop.getStopId())) {
				return KC_SID;
			} else if (SID_AU.equals(gStop.getStopId())) {
				return AU_SID;
			} else if (SID_NE.equals(gStop.getStopId())) {
				return NE_SID;
			} else if (SID_BD.equals(gStop.getStopId())) {
				return BD_SID;
			} else if (SID_BA.equals(gStop.getStopId())) {
				return BA_SID;
			} else if (SID_AD.equals(gStop.getStopId())) {
				return AD_SID;
			} else if (SID_MK.equals(gStop.getStopId())) {
				return MK_SID;
			} else if (SID_UI.equals(gStop.getStopId())) {
				return UI_SID;
			} else if (SID_MR.equals(gStop.getStopId())) {
				return MR_SID;
			} else if (SID_CE.equals(gStop.getStopId())) {
				return CE_SID;
			} else if (SID_MJ.equals(gStop.getStopId())) {
				return MJ_SID;
			} else if (SID_ST.equals(gStop.getStopId())) {
				return ST_SID;
			} else if (SID_LI.equals(gStop.getStopId())) {
				return LI_SID;
			} else if (SID_KE.equals(gStop.getStopId())) {
				return KE_SID;
			} else if (SID_WR.equals(gStop.getStopId())) {
				return WR_SID;
			} else if (SID_USBT.equals(gStop.getStopId())) {
				return USBT_SID;
			} else if (SID_NI.equals(gStop.getStopId())) {
				return NI_SID;
			} else if (SID_PA.equals(gStop.getStopId())) {
				return PA_SID;
			} else if (SID_SCTH.equals(gStop.getStopId())) {
				return SCTH_SID;
			} else if (SID_DW.equals(gStop.getStopId())) {
				return DW_SID;
			} else {
				System.out.println("Unexpected stop ID for " + gStop + "! (" + gStop.getStopId() + ")");
				System.exit(-1);
				return -1;
			}
		}
		return super.getStopId(gStop);
	}
}
