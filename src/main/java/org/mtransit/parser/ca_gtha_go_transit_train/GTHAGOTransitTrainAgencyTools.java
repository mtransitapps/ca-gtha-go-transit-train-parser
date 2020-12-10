package org.mtransit.parser.ca_gtha_go_transit_train;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
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

import java.util.HashSet;
import java.util.regex.Pattern;

// https://www.gotransit.com/en/information-resources/software-developers
// https://www.gotransit.com/fr/ressources-informatives/dveloppeurs-de-logiciel
// https://www.gotransit.com/static_files/gotransit/assets/Files/GO_GTFS.zip
public class GTHAGOTransitTrainAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-gtha-go-transit-train-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new GTHAGOTransitTrainAgencyTools().start(args);
	}

	@Nullable
	private HashSet<Integer> serviceIdInts;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating GO Transit train data...");
		long start = System.currentTimeMillis();
		this.serviceIdInts = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating GO Transit train data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIdInts != null && this.serviceIdInts.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIdInts);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIdInts);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (this.serviceIdInts != null) {
			return excludeUselessTripInt(gTrip, this.serviceIdInts);
		}
		return super.excludeTrip(gTrip);
	}

	@NotNull
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
	public long getRouteId(@NotNull GRoute gRoute) {
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
		//noinspection deprecation
		final String routeId = gRoute.getRouteId();
		if (routeId.endsWith(ST_RSN)) {
			return ST_RID;
		} else if (routeId.endsWith(RH_RSN)) {
			return RH_RID;
		} else if (routeId.endsWith(MI_RSN)) {
			return MI_RID;
		} else if (routeId.endsWith(LW_RSN)) {
			return LW_RID;
		} else if (routeId.endsWith(LE_RSN)) {
			return LE_RID;
		} else if (routeId.endsWith(KI_RSN) //
				|| routeId.endsWith(GT_RSN)) {
			return KI_RID;
		} else if (routeId.endsWith(BR_RSN)) {
			return BR_RID;
		}
		throw new MTLog.Fatal("Unexpected route ID for %s!", gRoute);
	}

	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
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

	@Nullable
	@Override
	public String getRouteShortName(@NotNull GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteShortName())) {
			//noinspection deprecation
			final String routeId = gRoute.getRouteId();
			if (routeId.endsWith(ST_RSN)) {
				return ST_RSN;
			} else if (routeId.endsWith(RH_RSN)) {
				return RH_RSN;
			} else if (routeId.endsWith(MI_RSN)) {
				return MI_RSN;
			} else if (routeId.endsWith(LW_RSN)) {
				return LW_RSN;
			} else if (routeId.endsWith(LE_RSN)) {
				return LE_RSN;
			} else if (routeId.endsWith(KI_RSN) //
					|| routeId.endsWith(GT_RSN)) {
				return GT_RSN;
			} else if (routeId.endsWith(BR_RSN)) {
				return BR_RSN;
			}
			throw new MTLog.Fatal("Unexpected route short name for %s!", gRoute);
		}
		return super.getRouteShortName(gRoute);
	}

	private static final String AGENCY_COLOR = "387C2B"; // GREEN (AGENCY WEB SITE CSS)

	@NotNull
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

	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
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
			throw new MTLog.Fatal("Unexpected route color '%s'!", gRoute);
		}
		return super.getRouteColor(gRoute);
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		mTrip.setHeadsignString(
				cleanTripHeadsign(gTrip.getTripHeadsign()),
				gTrip.getDirectionIdOrDefault()
		);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		throw new MTLog.Fatal("%s: Using direction finder to merge %s and %s!", mTrip.getRouteId(), mTrip, mTripToMerge);
	}

	private static final Pattern START_WITH_RSN = Pattern.compile("(^[A-Z]{2}(\\s+)- )", Pattern.CASE_INSENSITIVE);

	private static final Pattern FIRST_STATION_TIME_LAST_STATION_TIME = Pattern.compile("" //
			+ "(" //
			+ "([\\w\\s]*)" //
			+ "[\\s]+" //
			+ "([\\d]{2}:[\\d]{2})" //
			+ "[\\s]+" //
			+ "-" //
			+ "[\\s]+" //
			+ "([\\w\\s]*)" //
			+ "[\\s]+" //
			+ "([\\d]{2}:[\\d]{2})" //
			+ ")", Pattern.CASE_INSENSITIVE);
	private static final String FIRST_STATION_TIME_LAST_STATION_TIME_REPLACEMENT = "$4";

	private static final Pattern CENTER = Pattern.compile("((^|\\W)(center|centre|ctr)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String CENTER_REPLACEMENT = " ";

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = START_WITH_RSN.matcher(tripHeadsign).replaceAll(StringUtils.EMPTY);
		tripHeadsign = FIRST_STATION_TIME_LAST_STATION_TIME.matcher(tripHeadsign).replaceAll(FIRST_STATION_TIME_LAST_STATION_TIME_REPLACEMENT);
		tripHeadsign = GO.matcher(tripHeadsign).replaceAll(GO_REPLACEMENT);
		tripHeadsign = CENTER.matcher(tripHeadsign).replaceAll(CENTER_REPLACEMENT);
		tripHeadsign = STATION.matcher(tripHeadsign).replaceAll(STATION_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern GO = Pattern.compile("(^|\\W)(go)($|\\W)", Pattern.CASE_INSENSITIVE);
	private static final String GO_REPLACEMENT = " ";

	private static final Pattern VIA = Pattern.compile("(^|\\s)(via)($|\\s)", Pattern.CASE_INSENSITIVE);
	private static final String VIA_REPLACEMENT = " ";

	private static final Pattern RAIL = Pattern.compile("(^|\\s)(rail)($|\\s)", Pattern.CASE_INSENSITIVE);
	private static final String RAIL_REPLACEMENT = " ";

	private static final Pattern STATION = Pattern.compile("(^|\\s)(station)($|\\s)", Pattern.CASE_INSENSITIVE);
	private static final String STATION_REPLACEMENT = " ";

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
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
	public int getStopId(@NotNull GStop gStop) {
		//noinspection deprecation
		final String stopId = gStop.getStopId();
		if (!Utils.isDigitsOnly(stopId)) {
			//noinspection IfCanBeSwitch
			if (SID_UN.equals(stopId)) {
				return UN_SID;
			} else if (SID_EX.equals(stopId)) {
				return EX_SID;
			} else if (SID_MI.equals(stopId)) {
				return MI_SID;
			} else if (SID_LO.equals(stopId)) {
				return LO_SID;
			} else if (SID_DA.equals(stopId)) {
				return DA_SID;
			} else if (SID_SC.equals(stopId)) {
				return SC_SID;
			} else if (SID_EG.equals(stopId)) {
				return EG_SID;
			} else if (SID_GU.equals(stopId)) {
				return GU_SID;
			} else if (SID_RO.equals(stopId)) {
				return RO_SID;
			} else if (SID_PO.equals(stopId)) {
				return PO_SID;
			} else if (SID_CL.equals(stopId)) {
				return CL_SID;
			} else if (SID_OA.equals(stopId)) {
				return OA_SID;
			} else if (SID_BO.equals(stopId)) {
				return BO_SID;
			} else if (SID_AP.equals(stopId)) {
				return AP_SID;
			} else if (SID_BU.equals(stopId)) {
				return BU_SID;
			} else if (SID_AL.equals(stopId)) {
				return AL_SID;
			} else if (SID_PIN.equals(stopId)) {
				return PIN_SID;
			} else if (SID_AJ.equals(stopId)) {
				return AJ_SID;
			} else if (SID_WH.equals(stopId)) {
				return WH_SID;
			} else if (SID_OS.equals(stopId)) {
				return OS_SID;
			} else if (SID_BL.equals(stopId)) {
				return BL_SID;
			} else if (SID_KP.equals(stopId)) {
				return KP_SID;
			} else if (SID_WE.equals(stopId)) {
				return WE_SID;
			} else if (SID_ET.equals(stopId)) {
				return ET_SID;
			} else if (SID_OR.equals(stopId)) {
				return OR_SID;
			} else if (SID_OL.equals(stopId)) {
				return OL_SID;
			} else if (SID_AG.equals(stopId)) {
				return AG_SID;
			} else if (SID_DI.equals(stopId)) {
				return DI_SID;
			} else if (SID_CO.equals(stopId)) {
				return CO_SID;
			} else if (SID_ER.equals(stopId)) {
				return ER_SID;
			} else if (SID_HA.equals(stopId)) {
				return HA_SID;
			} else if (SID_YO.equals(stopId)) {
				return YO_SID;
			} else if (SID_SR.equals(stopId)) {
				return SR_SID;
			} else if (SID_ME.equals(stopId)) {
				return ME_SID;
			} else if (SID_LS.equals(stopId)) {
				return LS_SID;
			} else if (SID_ML.equals(stopId)) {
				return ML_SID;
			} else if (SID_KI.equals(stopId)) {
				return KI_SID;
			} else if (SID_MA.equals(stopId)) {
				return MA_SID;
			} else if (SID_BE.equals(stopId)) {
				return BE_SID;
			} else if (SID_BR.equals(stopId)) {
				return BR_SID;
			} else if (SID_MO.equals(stopId)) {
				return MO_SID;
			} else if (SID_GE.equals(stopId)) {
				return GE_SID;
			} else if (SID_GO.equals(stopId)) {
				return GO_SID;
			} else if (SID_AC.equals(stopId)) {
				return AC_SID;
			} else if (SID_GL.equals(stopId)) {
				return GL_SID;
			} else if (SID_EA.equals(stopId)) {
				return EA_SID;
			} else if (SID_LA.equals(stopId)) {
				return LA_SID;
			} else if (SID_RI.equals(stopId)) {
				return RI_SID;
			} else if (SID_MP.equals(stopId)) {
				return MP_SID;
			} else if (SID_RU.equals(stopId)) {
				return RU_SID;
			} else if (SID_KC.equals(stopId)) {
				return KC_SID;
			} else if (SID_AU.equals(stopId)) {
				return AU_SID;
			} else if (SID_NE.equals(stopId)) {
				return NE_SID;
			} else if (SID_BD.equals(stopId)) {
				return BD_SID;
			} else if (SID_BA.equals(stopId)) {
				return BA_SID;
			} else if (SID_AD.equals(stopId)) {
				return AD_SID;
			} else if (SID_MK.equals(stopId)) {
				return MK_SID;
			} else if (SID_UI.equals(stopId)) {
				return UI_SID;
			} else if (SID_MR.equals(stopId)) {
				return MR_SID;
			} else if (SID_CE.equals(stopId)) {
				return CE_SID;
			} else if (SID_MJ.equals(stopId)) {
				return MJ_SID;
			} else if (SID_ST.equals(stopId)) {
				return ST_SID;
			} else if (SID_LI.equals(stopId)) {
				return LI_SID;
			} else if (SID_KE.equals(stopId)) {
				return KE_SID;
			} else if (SID_WR.equals(stopId)) {
				return WR_SID;
			} else if (SID_USBT.equals(stopId)) {
				return USBT_SID;
			} else if (SID_NI.equals(stopId)) {
				return NI_SID;
			} else if (SID_PA.equals(stopId)) {
				return PA_SID;
			} else if (SID_SCTH.equals(stopId)) {
				return SCTH_SID;
			} else if (SID_DW.equals(stopId)) {
				return DW_SID;
			} else {
				throw new MTLog.Fatal("Unexpected stop ID for " + gStop + "! (" + stopId + ")");
			}
		}
		return super.getStopId(gStop);
	}
}
