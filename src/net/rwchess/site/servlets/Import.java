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
			else if (names[i].equals("pchesso")) rank = 3;
			
			pm.makePersistent(new RWMember(names[i], passwords[i], rank,
					countries[i]));
		}
		try {
			res.getOutputStream().println("Done!");
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DAO.flushMembersCache();
		DAO.flushSwissGuestCache();

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
