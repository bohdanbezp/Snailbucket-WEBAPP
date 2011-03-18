package net.rwchess.site.servlets;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;

import chesspresso.game.Game;
import chesspresso.pgn.PGNReader;
import chesspresso.pgn.PGNSyntaxError;
import net.rwchess.site.data.CacheObject;
import net.rwchess.site.data.DAO;
import net.rwchess.site.data.File;
import net.rwchess.site.data.RWMember;
import net.rwchess.site.data.RWSwissPlayer;
import net.rwchess.site.data.SwissGuest;
import net.rwchess.site.utils.Mailer;
import net.rwchess.site.utils.UsefulMethods;


/**
 * Used to import the preliminary data for once 
 */
public class Import extends HttpServlet {
	
	private static Map m;
	
	public static int getRatingFor(String username) {
		if (username.equalsIgnoreCase("Maras")) return 2236;
		else if (username.equalsIgnoreCase("roberttorma")) return 2229;
		else if (username.equalsIgnoreCase("PeterSanderson")) return 2187;
		else if (username.equalsIgnoreCase("iwulu")) return 2184;
		else if (username.equalsIgnoreCase("Karima")) return 2175;
		else if (username.equalsIgnoreCase("zalik")) return 2093;
		else if (username.equalsIgnoreCase("AIDog")) return 2086;
		else if (username.equalsIgnoreCase("drinkeh")) return 2084;
		else if (username.equalsIgnoreCase("Pallokala")) return 2066;
		else if (username.equalsIgnoreCase("Gregorioo")) return 2034;
		else if (username.equalsIgnoreCase("Madmansreturn")) return 1996;
		else if (username.equalsIgnoreCase("Harmonicus")) return 1961;
		else if (username.equalsIgnoreCase("jussu")) return 1953;
		else if (username.equalsIgnoreCase("ivohristov")) return 1943;
		else if (username.equalsIgnoreCase("AlesD")) return 1924;
		else if (username.equalsIgnoreCase("sangalla")) return 1911;
		else if (username.equalsIgnoreCase("NatIN")) return 1851;
		else if (username.equalsIgnoreCase("Acho")) return 1843;
		else if (username.equalsIgnoreCase("Treekiller")) return 1784;
		else if (username.equalsIgnoreCase("pchesso")) return 1775;
		else if (username.equalsIgnoreCase("NoiroP")) return 1759;
		else if (username.equalsIgnoreCase("bodzolca")) return 1731;
		else if (username.equalsIgnoreCase("WilkBardzoZly")) return 1673;
		else if (username.equalsIgnoreCase("Bodia")) return 1668;
		else if (username.equalsIgnoreCase("HerrGott")) return 1658;
		else if (username.equalsIgnoreCase("SachinRavi")) return 1655;
		else if (username.equalsIgnoreCase("Gavrilo")) return 1630;
		else if (username.equalsIgnoreCase("piorgovici")) return 1566;
		else if (username.equalsIgnoreCase("wfletcher")) return 1435;
		else if (username.equalsIgnoreCase("Nitreb")) return 1258;
		else if (username.equalsIgnoreCase("Credit")) return 2243;
		else if (username.equalsIgnoreCase("wuqi")) return 2227;
		else if (username.equalsIgnoreCase("clmnky")) return 2182;
		else if (username.equalsIgnoreCase("insertcoin")) return 2113;
		else if (username.equalsIgnoreCase("Alparslan")) return 1980;
		else if (username.equalsIgnoreCase("Rosadot")) return 1914;
		else if (username.equalsIgnoreCase("hectorabcd")) return 2331;
		else if (username.equalsIgnoreCase("thesixmax")) return 2302;
		else if (username.equalsIgnoreCase("dzomba")) return 2030;
		else if (username.equalsIgnoreCase("Leader")) return 2020;
		else if (username.equalsIgnoreCase("lingye")) return 1981;
		else if (username.equalsIgnoreCase("Inschipupa")) return 1923;
		else if (username.equalsIgnoreCase("alifant")) return 2130;
		else if (username.equalsIgnoreCase("squib")) return 2092;
		else if (username.equalsIgnoreCase("Rascian")) return 2012;
		else if (username.equalsIgnoreCase("Cuilin")) return 1997;
		else if (username.equalsIgnoreCase("LinceHispano")) return 1929;
		else if (username.equalsIgnoreCase("PlayOrangUtan")) return 2030;
		else if (username.equalsIgnoreCase("salero")) return 2036;
		else if (username.equalsIgnoreCase("rankzero")) return 2007;
		else if (username.equalsIgnoreCase("FESfW")) return 2009;
		else if (username.equalsIgnoreCase("NokiaTwenty")) return 1916;
		else if (username.equalsIgnoreCase("Kryssy")) return 1782;
		else if (username.equalsIgnoreCase("jaberwock")) return 2060;
		else if (username.equalsIgnoreCase("NBZ")) return 2034;
		else if (username.equalsIgnoreCase("Twikki")) return 2026;
		else if (username.equalsIgnoreCase("psykasso")) return 1922;
		else if (username.equalsIgnoreCase("nham")) return 1914;
		else if (username.equalsIgnoreCase("Amaterasu")) return 2148;
		else if (username.equalsIgnoreCase("ArtemZenit")) return 1992;
		else if (username.equalsIgnoreCase("Levenfish")) return 1960;
		else if (username.equalsIgnoreCase("Outposter")) return 1857;
		else if (username.equalsIgnoreCase("predkoo")) return 1845;
		else if (username.equalsIgnoreCase("stevelco")) return 1701;
		else if (username.equalsIgnoreCase("tjradd")) return 2204;
		else if (username.equalsIgnoreCase("jones")) return 1914;
		else if (username.equalsIgnoreCase("phlemish")) return 1925;
		else if (username.equalsIgnoreCase("xombie")) return 1901;
		else if (username.equalsIgnoreCase("tseltzer")) return 1885;
		else if (username.equalsIgnoreCase("FLORkarpov")) return 1827;
		else if (username.equalsIgnoreCase("insertcoin")) return 2113;
		else if (username.equalsIgnoreCase("Alparslan")) return 1980;
		else if (username.equalsIgnoreCase("cstarx")) return 1954;
		else if (username.equalsIgnoreCase("bjs")) return 1870;
		else if (username.equalsIgnoreCase("muwwatalli")) return 1904;
		else if (username.equalsIgnoreCase("gaelleg")) return 2183;
		else if (username.equalsIgnoreCase("FDog")) return 2028;
		else if (username.equalsIgnoreCase("evilcoyote")) return 1955;
		else if (username.equalsIgnoreCase("pbeccari")) return 1750;
		else if (username.equalsIgnoreCase("CebirX")) return 1769;
		else if (username.equalsIgnoreCase("sonareclipse")) return 1979;
		else if (username.equalsIgnoreCase("smallblackcat")) return 1989;
		else if (username.equalsIgnoreCase("xivarmy")) return 1936;
		else if (username.equalsIgnoreCase("maeck")) return 1901;
		else if (username.equalsIgnoreCase("Doubleletter")) return 1727;
		else if (username.equalsIgnoreCase("GreatSachin")) return 1678;
		else if (username.equalsIgnoreCase("Delenn")) return 2064;
		else if (username.equalsIgnoreCase("pramado")) return 1947;
		else if (username.equalsIgnoreCase("lingzhi")) return 1914;
		else if (username.equalsIgnoreCase("Valiantangel")) return 1861;
		else if (username.equalsIgnoreCase("Lir")) return 1811;
		else if (username.equalsIgnoreCase("sibreen")) return 1710;
		else if (username.equalsIgnoreCase("Pitigrilli")) return 2044;
		else if (username.equalsIgnoreCase("ShakaZahn")) return 1954;
		else if (username.equalsIgnoreCase("StiliDimitrov")) return 1935;
		else if (username.equalsIgnoreCase("BronsteinTheBest")) return 1831;
		else if (username.equalsIgnoreCase("LaurentiuI")) return 1799;
		else if (username.equalsIgnoreCase("nemotyro")) return 1643;
		else if (username.equalsIgnoreCase("Tohbie")) return 1917;
		else if (username.equalsIgnoreCase("NokiaTwenty")) return 1916;
		else if (username.equalsIgnoreCase("Drzej")) return 1954;
		else if (username.equalsIgnoreCase("beuki")) return 1902;
		else if (username.equalsIgnoreCase("Kryssy")) return 1782;
		else if (username.equalsIgnoreCase("linkinkev")) return 1792;
		else if (username.equalsIgnoreCase("Boedi")) return 1985;
		else if (username.equalsIgnoreCase("BusDriver")) return 1950;
		else if (username.equalsIgnoreCase("Chessstyles")) return 1887;
		else if (username.equalsIgnoreCase("DeanJohnson")) return 1781;
		else if (username.equalsIgnoreCase("Knoeier")) return 1744;
		else if (username.equalsIgnoreCase("ValenceJordan")) return 2049;
		else if (username.equalsIgnoreCase("DragonAce")) return 1921;
		else if (username.equalsIgnoreCase("Arut")) return 1851;
		else if (username.equalsIgnoreCase("brenopimenta")) return 1777;
		else if (username.equalsIgnoreCase("Gianuzio")) return 1760;
		else if (username.equalsIgnoreCase("CyrusX")) return 1728;
		else if (username.equalsIgnoreCase("Pawnadian")) return 1947;
		else if (username.equalsIgnoreCase("danijelo")) return 1942;
		else if (username.equalsIgnoreCase("Mekk")) return 1848;
		else if (username.equalsIgnoreCase("GroundControl")) return 1836;
		else if (username.equalsIgnoreCase("Fashion")) return 1806;
		else if (username.equalsIgnoreCase("TGV")) return 1757;
		else if (username.equalsIgnoreCase("hugozver")) return 1899;
		else if (username.equalsIgnoreCase("Blondie")) return 1915;
		else if (username.equalsIgnoreCase("sydbarretlives")) return 1859;
		else if (username.equalsIgnoreCase("toweronika")) return 1778;
		else if (username.equalsIgnoreCase("Sweetness")) return 1703;
		else if (username.equalsIgnoreCase("Boriqua")) return 1560;
		else if (username.equalsIgnoreCase("Technika")) return 2078;
		else if (username.equalsIgnoreCase("erusin")) return 1859;
		else if (username.equalsIgnoreCase("lastchancexi")) return 1761;
		else if (username.equalsIgnoreCase("BethanyGrace")) return 1685;
		else if (username.equalsIgnoreCase("clarinetref")) return 1662;
		else if (username.equalsIgnoreCase("RedPimpernel")) return 1609;
		else if (username.equalsIgnoreCase("Takhisis")) return 1904;
		else if (username.equalsIgnoreCase("shivaa")) return 1951;
		else if (username.equalsIgnoreCase("thatgirl")) return 1750;
		else if (username.equalsIgnoreCase("RamMohan")) return 1769;
		else if (username.equalsIgnoreCase("darkchocolate")) return 1677;
		else if (username.equalsIgnoreCase("kemoslobby")) return 1916;
		else if (username.equalsIgnoreCase("LostIllusion")) return 1865;
		else if (username.equalsIgnoreCase("cadger")) return 1824;
		else if (username.equalsIgnoreCase("fujin")) return 1737;
		else if (username.equalsIgnoreCase("GreatSachin")) return 1678;
		else if (username.equalsIgnoreCase("Marcinos")) return 2046;
		else if (username.equalsIgnoreCase("Vishyy")) return 1875;
		else if (username.equalsIgnoreCase("Qku")) return 1727;
		else if (username.equalsIgnoreCase("wojtekzam")) return 1725;
		else if (username.equalsIgnoreCase("tepepe")) return 1703;
		else if (username.equalsIgnoreCase("kocur")) return 1564;
		else if (username.equalsIgnoreCase("wmahan")) return 1933;
		else if (username.equalsIgnoreCase("LaurentiuI")) return 1799;
		else if (username.equalsIgnoreCase("milpat")) return 1827;
		else if (username.equalsIgnoreCase("Henderb")) return 1746;
		else if (username.equalsIgnoreCase("shivaroxxx")) return 1684;
		else if (username.equalsIgnoreCase("nham")) return 1914;
		else if (username.equalsIgnoreCase("Amauta")) return 1851;
		else if (username.equalsIgnoreCase("cjldx")) return 1796;
		else if (username.equalsIgnoreCase("fernbap")) return 1724;
		else if (username.equalsIgnoreCase("silversteel")) return 1756;
		else if (username.equalsIgnoreCase("muwwatalli")) return 1904;
		else if (username.equalsIgnoreCase("Estranger")) return 1803;
		else if (username.equalsIgnoreCase("Thryge")) return 1804;
		else if (username.equalsIgnoreCase("utaktaho")) return 1753;
		else if (username.equalsIgnoreCase("ThePawnBreak")) return 1563;
		else if (username.equalsIgnoreCase("lingzhi")) return 1914;
		else if (username.equalsIgnoreCase("ButiOxa")) return 1807;
		else if (username.equalsIgnoreCase("sibreen")) return 1710;
		else if (username.equalsIgnoreCase("gile")) return 1698;
		else if (username.equalsIgnoreCase("Yakusoku")) return 1695;
		else if (username.equalsIgnoreCase("CelticDeath")) return 1667;
		else if (username.equalsIgnoreCase("Takhisis")) return 1904;
		else if (username.equalsIgnoreCase("fujin")) return 1737;
		else if (username.equalsIgnoreCase("azonips")) return 1773;
		else if (username.equalsIgnoreCase("ChickenBrad")) return 1677;
		else if (username.equalsIgnoreCase("SeanBernardino")) return 1642;
		else if (username.equalsIgnoreCase("Dhumavati")) return 1259;
		else if (username.equalsIgnoreCase("Spelbreker")) return 1866;
		else if (username.equalsIgnoreCase("Caballos")) return 1799;
		else if (username.equalsIgnoreCase("volgjeneus")) return 1822;
		else if (username.equalsIgnoreCase("irWietje")) return 1593;
		else if (username.equalsIgnoreCase("thefourthhorseman")) return 1501;
		else if (username.equalsIgnoreCase("Inschipupa")) return 1923;
		else if (username.equalsIgnoreCase("TGV")) return 1757;
		else if (username.equalsIgnoreCase("LLIAMAH")) return 1688;
		else if (username.equalsIgnoreCase("areuh")) return 1687;
		else if (username.equalsIgnoreCase("Galleta")) return 1677;
		else if (username.equalsIgnoreCase("bvrus")) return 1624;
		else if (username.equalsIgnoreCase("olechos")) return 1776;
		else if (username.equalsIgnoreCase("witor")) return 1774;
		else if (username.equalsIgnoreCase("PankracyRozumek")) return 1746;
		else if (username.equalsIgnoreCase("Ivanuk")) return 1740;
		else if (username.equalsIgnoreCase("stevelco")) return 1701;
		else if (username.equalsIgnoreCase("RockyRook")) return 1625;
		else if (username.equalsIgnoreCase("Vishyy")) return 1875;
		else if (username.equalsIgnoreCase("wojtekzam")) return 1725;
		else if (username.equalsIgnoreCase("Qku")) return 1727;
		else if (username.equalsIgnoreCase("tepepe")) return 1703;
		else if (username.equalsIgnoreCase("rutra")) return 1596;
		else if (username.equalsIgnoreCase("kocur")) return 1564;
		else if (username.equalsIgnoreCase("LinceHispano")) return 1929;
		else if (username.equalsIgnoreCase("Mahog")) return 1751;
		else if (username.equalsIgnoreCase("GreatSachin")) return 1678;
		else if (username.equalsIgnoreCase("onomatopeia")) return 1648;
		else if (username.equalsIgnoreCase("stalebread")) return 1595;
		else if (username.equalsIgnoreCase("Tonyse")) return 1799;
		else if (username.equalsIgnoreCase("arunsingh")) return 1735;
		else if (username.equalsIgnoreCase("lastchancexi")) return 1761;
		else if (username.equalsIgnoreCase("Nestore")) return 1696;
		else if (username.equalsIgnoreCase("jvonhelf")) return 1679;
		else if (username.equalsIgnoreCase("tightfist")) return 1615;
		else if (username.equalsIgnoreCase("pchesso")) return 1775;
		else if (username.equalsIgnoreCase("NoiroP")) return 1759;
		else if (username.equalsIgnoreCase("bodzolca")) return 1731;
		else if (username.equalsIgnoreCase("WilkBardzoZly")) return 1673;
		else if (username.equalsIgnoreCase("Bodia")) return 1668;
		else if (username.equalsIgnoreCase("piorgovici")) return 1566;
		else if (username.equalsIgnoreCase("uuddlrlrba")) return 1888;
		else if (username.equalsIgnoreCase("Pensphan")) return 1793;
		else if (username.equalsIgnoreCase("ahorse")) return 1614;
		else if (username.equalsIgnoreCase("JoshuaR")) return 1625;
		else if (username.equalsIgnoreCase("AlexDonovan")) return 1195;
		else if (username.equalsIgnoreCase("Leucky")) return 1807;
		else if (username.equalsIgnoreCase("linkinkev")) return 1792;
		else if (username.equalsIgnoreCase("duszek")) return 1722;
		else if (username.equalsIgnoreCase("ghp")) return 1569;
		else if (username.equalsIgnoreCase("philk")) return 1536;
		else if (username.equalsIgnoreCase("Saglitz")) return 1370;
		else if (username.equalsIgnoreCase("Fashion")) return 1806;
		else if (username.equalsIgnoreCase("clarinetref")) return 1662;
		else if (username.equalsIgnoreCase("pmsd")) return 1659;
		else if (username.equalsIgnoreCase("ArturPL")) return 1644;
		else if (username.equalsIgnoreCase("jsobo")) return 1587;
		else if (username.equalsIgnoreCase("RoyRogersC")) return 1566;
		else if (username.equalsIgnoreCase("zitterbart")) return 1709;
		else if (username.equalsIgnoreCase("AdisFKN")) return 1659;
		else if (username.equalsIgnoreCase("Malfurion")) return 1702;
		else if (username.equalsIgnoreCase("NobisPacem")) return 1692;
		else if (username.equalsIgnoreCase("Wollahs")) return 1615;
		else if (username.equalsIgnoreCase("yanpaulo")) return 1573;
		else if (username.equalsIgnoreCase("darnakas")) return 1795;
		else if (username.equalsIgnoreCase("kambodjaa")) return 1678;
		else if (username.equalsIgnoreCase("CarlosKerber")) return 1651;
		else if (username.equalsIgnoreCase("DodgeBrother")) return 1560;
		else if (username.equalsIgnoreCase("uhcaldeirao")) return 1538;
		else if (username.equalsIgnoreCase("uragano")) return 1610;
		else if (username.equalsIgnoreCase("Dwagner")) return 1763;
		else if (username.equalsIgnoreCase("Knoeier")) return 1744;
		else if (username.equalsIgnoreCase("LightKnight")) return 1630;
		else if (username.equalsIgnoreCase("chaostheory")) return 1519;
		else if (username.equalsIgnoreCase("cofail")) return 1747;
		else if (username.equalsIgnoreCase("GreatSachin")) return 1678;
		else if (username.equalsIgnoreCase("ThePawnBreak")) return 1563;
		else if (username.equalsIgnoreCase("alekseju")) return 1513;
		else if (username.equalsIgnoreCase("ducetray")) return 1333;
		else if (username.equalsIgnoreCase("CelticDeath")) return 1667;
		else if (username.equalsIgnoreCase("KillerWolf")) return 1641;
		else if (username.equalsIgnoreCase("SamuraiGoroh")) return 1627;
		else if (username.equalsIgnoreCase("LeifPetersen")) return 1524;
		else if (username.equalsIgnoreCase("searchingforgreg")) return 1410;
		else if (username.equalsIgnoreCase("SecondQueen")) return 1307;
		else if (username.equalsIgnoreCase("Papaflesas")) return 1794;
		else if (username.equalsIgnoreCase("Berke")) return 1594;
		else if (username.equalsIgnoreCase("petlya")) return 1529;
		else if (username.equalsIgnoreCase("freefal")) return 1527;
		else if (username.equalsIgnoreCase("andrejas")) return 1409;
		else if (username.equalsIgnoreCase("piejei")) return 1414;
		else if (username.equalsIgnoreCase("NovoLook")) return 1691;
		else if (username.equalsIgnoreCase("ccaseiro")) return 1611;
		else if (username.equalsIgnoreCase("ikkuhss")) return 1609;
		else if (username.equalsIgnoreCase("kmindzero")) return 1532;
		else if (username.equalsIgnoreCase("blkmagic")) return 1569;
		else if (username.equalsIgnoreCase("OldFlyer")) return 1499;
		else if (username.equalsIgnoreCase("BethanyGrace")) return 1685;
		else if (username.equalsIgnoreCase("silencehunter")) return 1621;
		else if (username.equalsIgnoreCase("RoyRogersC")) return 1566;
		else if (username.equalsIgnoreCase("CNoble")) return 1557;
		else if (username.equalsIgnoreCase("timisrejoicing")) return 1501;
		else if (username.equalsIgnoreCase("LightKnight")) return 1630;
		else if (username.equalsIgnoreCase("nemotyro")) return 1643;
		else if (username.equalsIgnoreCase("SCUGrad")) return 1589;
		else if (username.equalsIgnoreCase("bazinga")) return 1554;
		else if (username.equalsIgnoreCase("apetrescu")) return 1460;
		else if (username.equalsIgnoreCase("GreatSachin")) return 1678;
		else if (username.equalsIgnoreCase("nubie")) return 1616;
		else if (username.equalsIgnoreCase("jariv")) return 1553;
		else if (username.equalsIgnoreCase("foxchaseii")) return 1538;
		else if (username.equalsIgnoreCase("gorckat")) return 1499;
		else if (username.equalsIgnoreCase("dstrout")) return 1678;
		else if (username.equalsIgnoreCase("onomatopeia")) return 1648;
		else if (username.equalsIgnoreCase("mauriecain")) return 1543;
		else if (username.equalsIgnoreCase("zulugodetia")) return 1512;
		else if (username.equalsIgnoreCase("allencox")) return 1511;
		else if (username.equalsIgnoreCase("rutra")) return 1596;
		else if (username.equalsIgnoreCase("uglyandy")) return 1577;
		else if (username.equalsIgnoreCase("Sszymek")) return 1564;
		else if (username.equalsIgnoreCase("kocur")) return 1564;
		else if (username.equalsIgnoreCase("Spartanix")) return 1490;
		else if (username.equalsIgnoreCase("mjb")) return 1711;
		else if (username.equalsIgnoreCase("SWLL")) return 1568;
		else if (username.equalsIgnoreCase("thehippo")) return 1525;
		else if (username.equalsIgnoreCase("djort")) return 1493;
		else if (username.equalsIgnoreCase("nicolaus")) return 1454;
		else if (username.equalsIgnoreCase("carnada")) return 1436;
		else if (username.equalsIgnoreCase("SachinRavi")) return 1655;
		else if (username.equalsIgnoreCase("Gavrilo")) return 1630;
		else if (username.equalsIgnoreCase("piorgovici")) return 1566;
		else if (username.equalsIgnoreCase("wfletcher")) return 1435;
		else if (username.equalsIgnoreCase("Nitreb")) return 1258;
		else if (username.equalsIgnoreCase("RedPimpernel")) return 1609;
		else if (username.equalsIgnoreCase("yanpaulo")) return 1573;
		else if (username.equalsIgnoreCase("twotowers")) return 1542;
		else if (username.equalsIgnoreCase("nasmichael")) return 1516;
		else if (username.equalsIgnoreCase("NikosGr")) return 1463;
		else if (username.equalsIgnoreCase("poiitis")) return 1332;
		else if (username.equalsIgnoreCase("pbeccari")) return 1750;
		else if (username.equalsIgnoreCase("foxchaseii")) return 1538;
		else if (username.equalsIgnoreCase("huisintveld")) return 1533;
		else if (username.equalsIgnoreCase("kosu")) return 1417;
		else if (username.equalsIgnoreCase("NoelTheHacker")) return 1396;
		else if (username.equalsIgnoreCase("gilmarbeta")) return 1336;
		else if (username.equalsIgnoreCase("schoorl")) return 1592;
		else if (username.equalsIgnoreCase("Introspection")) return 1558;
		else if (username.equalsIgnoreCase("Dreadtower")) return 1565;
		else if (username.equalsIgnoreCase("ExirK")) return 1497;
		else if (username.equalsIgnoreCase("InvisibleDog")) return 1337;
		else if (username.equalsIgnoreCase("EyeLikePie")) return 1621;
		else if (username.equalsIgnoreCase("ThePawnBreak")) return 1563;
		else if (username.equalsIgnoreCase("thehippo")) return 1525;
		else if (username.equalsIgnoreCase("dragongr")) return 1464;
		else if (username.equalsIgnoreCase("nicolaus")) return 1454;
		else if (username.equalsIgnoreCase("ducetray")) return 1333;
		else if (username.equalsIgnoreCase("jvonhelf")) return 1679;
		else if (username.equalsIgnoreCase("tightfist")) return 1615;
		else if (username.equalsIgnoreCase("IMOEC")) return 1475;
		else if (username.equalsIgnoreCase("unceasinggnome")) return 1402;
		else if (username.equalsIgnoreCase("plaandrew")) return 1333;
		else if (username.equalsIgnoreCase("RoyRogersC")) return 1566;
		else if (username.equalsIgnoreCase("timisrejoicing")) return 1501;
		else if (username.equalsIgnoreCase("xraychess")) return 1445;
		else if (username.equalsIgnoreCase("CharityJoy")) return 1449;
		else if (username.equalsIgnoreCase("CactusJr")) return 1404;
		else if (username.equalsIgnoreCase("littlesparrows")) return 1025;
		else if (username.equalsIgnoreCase("areuh")) return 1687;
		else if (username.equalsIgnoreCase("aditinitya")) return 1468;
		else if (username.equalsIgnoreCase("Markev")) return 1402;
		else if (username.equalsIgnoreCase("ZfromDtownCO")) return 1328;
		else if (username.equalsIgnoreCase("redpiggy")) return 1349;
		else if (username.equalsIgnoreCase("dsueiro")) return 1333;
		else return 0;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {	
		
	/*	String[] names = { "PeterSanderson", "iwulu", "Maras", "Gregorioo",
				"ivohristov", "AlesD", "Acho", "NoiroP", "bodzolca", "Bodia",
				"WilkBardzoZly", "pchesso", "Gavrilo", "sachinravi", "piorgovici",
				"wfletcher", "Nitreb"}; */
    
	/**	
		String toCommit = "[Event \"RW Swiss 2010\"]\n[Site \"FICS\"]\n[Date \"2010.03.28\"]\n[Round \"1\"]\n[White \"iwulu\"]\n[Black \"pchesso\"]\n[Result \"1-0\"]\n[WhiteElo \"2171\"]\n[BlackElo \"1720\"]\n[ECO \"A09\"]\n1. Nf3 d5 2. c4 d4 3. g3 c5 4. Bg2 Nc6 5. O-O e5 6. d3 Nf6 7. Nbd2 Be7 8. a3 a5 9. Rb1 O-O 10. Ne1 Qc7 11. Nc2 Nd7 12. e3 Ra6 13. exd4 cxd4 14. b4 axb4 15. axb4 Nd8 16. Na3 b6 17. Nb5 Qb8 18. f4 Ne6 19. Ne4 f5 20. Ng5 Nxg5 21. fxg5 Bb7 22. Qe2 Ra4 23. Nxd4 Bxg2 24. Qxg2 exd4 25. Qd5+ Rf7 26. Rxf5 Nf6 27. gxf6 Bxf6 28. Bg5 Qe8 29. Rbf1 Kh8 30. Bxf6 gxf6 31. Qxd4 {Black resigns} 1-0\n\n[Event \"RW Swiss 2010\"]\n[Site \"FICS\"]\n[Date \"2010.03.25\"]\n[Round \"1\"]\n[White \"bodzolca\"]\n[Black \"FriendshipFighter\"]\n[Result \"0-1\"]\n[WhiteElo \"1759\"]\n[BlackElo \"2128\"]\n[ECO \"A43\"]\n1. e4 d6 2. d4 g6 3. Nc3 Bg7 4. Nf3 c5 5. d5 Nf6 6. Bd3 O-O 7. O-O e6 8. Bg5 Na6 9. Qd2 Nc7 10. Bc4 exd5 11. exd5 Bg4 12. Nh4 a6 13. a4 b6 14. h3 Bd7 15. Rfe1 b5 16. axb5 axb5 17. Rxa8 Qxa8 18. Qf4 bxc4 19. Bxf6 Nxd5 20. Nxd5 Qxd5 21. Re7 Bc6 22. Nf3 h6 23. Bxg7 Kxg7 24. Qd2 Qxd2 25. Nxd2 Kf6 26. Rc7 Bd5 27. Ra7 Rb8 28. Ra6 Ke6 {White resigns} 0-1\n\n[Event \"RW Swiss 2010\"]\n[Site \"FICS\"]\n[Date \"2010.03.21\"]\n[Round \"1\"]\n[White \"Gregorioo\"]\n[Black \"Bodia\"]\n[Result \"1-0\"]\n[WhiteElo \"2072\"]\n[BlackElo \"1673\"]\n[ECO \"A36\"]\n1. c4 c5 2. g3 g6 3. Bg2 Bg7 4. Nc3 Nc6 5. d3 d6 6. h4 a6 7. h5 Nh6 8. Bg5 f6 9. Bd2 Rg8 10. hxg6 Nf5 11. e4 Nfd4 12. gxh7 Rh8 13. Qh5+ Kd7 14. Nd5 e6 15. Qf7+ {Black resigns} 1-0\n\n[Event \"RW Swiss 2010\"]\n[Site \"FICS\"]\n[Date \"2010.03.20\"]\n[Round \"1\"]\n[White \"SachinRavi\"]\n[Black \"Madmansreturn\"]\n[Result \"0-1\"]\n[WhiteElo \"1636\"]\n[BlackElo \"2004\"]\n[ECO \"B17\"]\n1. e4 c6 2. d4 d5 3. Nc3 dxe4 4. Nxe4 Nd7 5. Nf3 Ngf6 6. Bd3 Nxe4 7. Bxe4 Nf6 8. Bd3 Bg4 9. Be3 e6 10. O-O Bd6 11. h3 Bh5 12. Be2 Qc7 13. c4 O-O 14. c5 Bf4 15. Bxf4 Qxf4 16. Re1 Rfd8 17. Qb3 Rab8 18. Qa4 Bxf3 19. Bxf3 Rxd4 20. Qxa7 Ra4 21. g3 Rxa7 22. gxf4 Ra4 23. f5 Rf4 24. Bh1 Rxf5 25. b4 Rd8 26. Rac1 Rd2 27. Rcd1 Rfxf2 28. Bf3 Kf8 29. Be2 Rfxe2 30. Rxe2 Rxd1+ 31. Kf2 Nd5 32. Re4 Rd2+ 33. Kf3 Rxa2 34. Rh4 Ra3+ 35. Ke4 Re3+ 36. Kd4 h6 37. Rh5 e5+ 38. Rxe5 Rxe5 39. Kxe5 {White resigns} 0-1\n\n[Event \"RW Swiss 2010\"]\n[Site \"FICS\"]\n[Date \"2010.03.27\"]\n[Round \"1\"]\n[White \"sangalla\"]\n[Black \"Nitreb\"]\n[Result \"1-0\"]\n[WhiteElo \"1893\"]\n[BlackElo \"1244\"]\n[ECO \"D36\"]\n1. d4 d5 2. c4 e6 3. Nc3 Be7 4. Nf3 Nf6 5. Bg5 O-O 6. e3 c6 7. Qc2 Nbd7 8. cxd5 exd5 9. Bd3 h6 10. Bh4 Re8 11. O-O Nf8 12. Rab1 Bg4 13. Ne5 Be6 14. b4 N6d7 15. Bxe7 Qxe7 16. Nxd7 Nxd7 17. b5 c5 18. dxc5 Nxc5 19. Rfd1 Red8 20. Ne2 Rac8 21. Qd2 Ne4 22. Bxe4 dxe4 23. Nd4 Bc4 24. Qb2 Bd3 25. Rbc1 Qd7 26. h3 Qd5 27. a4 Rc4 28. Rxc4 Qxc4 29. Rc1 Qd5 30. Qb4 Qd7 31. Qc5 a6 32. Qc7 axb5 33. axb5 Kf8 34. b6 Ba6 35. Nf5 Qd5 36. Rc5 Qd7 37. Qxd7 Rxd7 38. Rc8+ 1-0";
		
			PersistenceManager pm = DAO.get().getPersistenceManager();
		try {
			File fl = (File) pm.getObjectById(File.class,
					"swiss2010.pgn");
			fl.setFile(new Blob(toCommit.getBytes()));
		} 
		catch (JDOObjectNotFoundException e) {
			
		}
		finally {
			pm.close();
		}*/
		
		
		
		getSwissParticipantsHtml(DAO.getSwissParticipants(),
					DAO.getAllPlayers(), DAO.getSwissGuests(),req,res);
		
		/*
		PersistenceManager pm = DAO.get().getPersistenceManager();
		String[] names = { "HerrGott", "Noiro", "piorgovici", "pchesso",
				"Bodia", "Acho", "sachinravi", "jussu", "Natin", "Nitreb",
				"roberttorma", "WilkBardzoZly", "iwulu", "wfletcher", "ivohristov",
				"Maras", "AlesD", "exray", "Gavrilo", "Pallokala", "bodzolca",
				"sangalla" };
		String[] countries = { "ro", "sk", "ro", "de", "ua", "ar", "in", "ee",
				"no", "ca", "hu", "pl", "ng", "za", "bg", "lt", "cz", "" +
						"ca", "cs", "fi", "si", "id" };
		String[] passwords = { "283ffefecd9c77eaac17eb510e0d0fde",
				"c098a4d9bb9516a951b7b510a76418b4",
				"c5258d384b2c9395cc56d0fa9f481306",
				"ce321c24dc777c81666271b4b78bc063",
				"44553e42030473c29b270fe3b1f728be",
				"a28710fcf793cd2374ac0c081e5c3f7d",
				"99693a548357e4b089837816c182a500",
				"d63d20e7ee8cc0dcfd68c038274945a2",
				"098f6bcd4621d373cade4e832627b4f6",
				"801d1b0502f760db02b6e690b0037430",				
				"968b18793e56cbea70692fba31189ae7",
				"3228d24e2ccc9443e82e58d5008c50f3",
				"10ce72c6b816ac8b25b062ebae2108ae",
				"74790f436b9dc6ae4d47bfb6c924d3ad",
				"95cbc4d8d2c2864de256fc08ce23d8c1",
				"7476ed9af142c6fe337846f0c5ac466b",
				"ea2b2676c28c0db26d39331a336c6b92",
				"64719db2fb744db5b11e76a5288323cf",
				"148de99d1e9f33f8ba3f8e0593730413",
				"ca209002fada69add4520c1532bd0ee3",
				"78c6d9c637aeaf5d3fd0be1220ed841e",
				"0aeeeb12859935e447391ce0750788de" };

		for (int i = 0; i < names.length; i++) {
			int rank = 1;
			if (names[i].equals("Bodia")) rank = 3;
			else if (names[i].equals("pchesso")) rank = 2;
			
			pm.makePersistent(new RWMember(names[i], passwords[i], rank,
					countries[i]));
		}
		try {
			res.getOutputStream().println("Done!");
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	private void getSwissParticipantsHtml(
			List<RWSwissPlayer> allPlayers, 
			List<RWMember> allMembers, List<SwissGuest> allGuests,
			HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		StringBuffer buff = new StringBuffer();
		int i = 0;
		int size = allPlayers.size();
		if (size%2 == 0)
			size = size/2;
		else
			size = (size-1)/2 + 1;
		
		while (size > i) {
			RWSwissPlayer pl1 = allPlayers.get(i);
			RWSwissPlayer pl2;			
			
			if (allPlayers.size() == (i+size)) {
				pl2 = new RWSwissPlayer();
				pl2.setFixedRating(0);
				pl2.setUsername("");
			}
			else
				pl2 = allPlayers.get(i+size);
			
			String country1 = null;
			boolean guest1 = false;
			for (RWMember uc: allMembers) {
				if (uc.getUsername().equalsIgnoreCase(pl1.getUsername()))
					country1 = uc.getCountry();
			}
			if (country1 == null) {
				for (SwissGuest uc: allGuests) {
					if (uc.getUsername().equalsIgnoreCase(pl1.getUsername())) {
						country1 = uc.getCountry();
						guest1 = true;
					}
				}
			}
			
			
			String country2 = "";
			boolean guest2 = false;
			if (!pl2.getUsername().isEmpty()) {
				for (RWMember uc : allMembers) {
					if (uc.getUsername().equalsIgnoreCase(pl2.getUsername()))
						country2 = uc.getCountry();
				}
				if (country2.isEmpty()) {
					for (SwissGuest uc : allGuests) {
						System.out.println("SwissGuest: [" + uc.getUsername()+"]");
						res.getOutputStream().println("SwissGuest: [" + uc.getUsername()+"]");
						System.out.println("RWSwissPlayer: [" + pl2.getUsername()+"]");
						res.getOutputStream().println("RWSwissPlayer: [" + pl2.getUsername()+"]");
						if (uc.getUsername().equalsIgnoreCase(pl2.getUsername())) {
							country2 = uc.getCountry();
							guest2 = true;
						}
					}
				}
			}
			
			
			i++;
		}
		
	}
}
