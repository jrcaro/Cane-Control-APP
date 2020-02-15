package com.example.canecontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static java.lang.StrictMath.sqrt;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int BATT_PROFILE_CONNECTED = 22;
    private static final int BATT_PROFILE_DISCONNECTED = 23;

    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BatteryService battService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private TextView textv, textBatt;
    private Button btn_connect, btn_send, btn_debug;
    private RadioButton radioFSM, radioNN, radioLF, radioHF;
    private RadioGroup radioG, radioF;
    private ProgressBar batLevelBar;
    private Toolbar mActionBarToolbar;
    int counter = 0;
    byte system_sel, freq_sel;
    private Fsm stateMachine;

    SimpleDateFormat dateStr = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    SimpleDateFormat dateFile = new SimpleDateFormat("dd-MM-yyyy");
    String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CaneControl";
    File dir;
    File file_sensor;
    File file_bat;
    String fName_sensor;
    String fName_bat;
    double[] nnArray = {0.227329494905601, 0.229091984821941, 0.230878069469333, 0.232690342236263, 0.234531455414513, 0.236404121851540, 0.238311116647117, 0.240255278873311,
            0.242239513295080, 0.244266792066760, 0.246340156377481, 0.248462718016071, 0.250637660823316, 0.252868241996398, 0.255157793207116, 0.257509721491922,
            0.259927509867979, 0.262414717625345, 0.264974980240993, 0.267612008855757, 0.270329589250423, 0.273131580252130, 0.276021911497048, 0.279004580470030,
            0.282083648736660, 0.285263237277954, 0.288547520833011, 0.291940721150389, 0.295447099044904, 0.299070945153332, 0.302816569280182, 0.306688288223690,
            0.310690411972703, 0.314827228167530, 0.319102984722472, 0.323521870515034, 0.328087994057128, 0.332805360077375, 0.337677843961258, 0.342709164017841,
            0.347902851568427, 0.353262218884109, 0.358790325036127, 0.364489939765133, 0.370363505523237, 0.376413097895539, 0.382640384665672, 0.389046583851843,
            0.395632421105249, 0.402398086930184, 0.409343194253326, 0.416466736936553, 0.423767049891171, 0.431241771509080, 0.438887809175493, 0.446701308665470,
            0.454677628249683, 0.462811318340725, 0.471096107496961, 0.479524895564201, 0.488089754674283, 0.496781938733044, 0.505591901917544, 0.514509326564752,
            0.523523160672862, 0.532621665055022, 0.541792469987747, 0.551022640987843, 0.560298753138665, 0.569606973175738, 0.578933148340884, 0.588262900830238,
            0.597581726502443, 0.606875096385278, 0.616128559427652, 0.625327844893520, 0.634458962787284, 0.643508300737602, 0.652462715846993, 0.661309620135401,
            0.670037058362292, 0.678633777198093, 0.687089284924478, 0.695393901066720, 0.703538795591660, 0.711516017534112, 0.719318513134991, 0.726940133779743,
            0.734375634209982, 0.741620661640356, 0.748671736543708, 0.755526225968711, 0.762182310325237, 0.768638944614458, 0.774895815094899, 0.780953292365012,
            0.786812381810470, 0.792474672313813, 0.797942284059032, 0.803217816187852, 0.808304294981366, 0.813205123153559, 0.817924030755041, 0.822465028098399,
            0.826832361033108, 0.831030468819502, 0.835063944778953, 0.838937499832186, 0.842655928979767, 0.846224080728584, 0.849646829425451, 0.852929050423473,
            0.856075597978194, 0.859091285748198, 0.861980869758144, 0.864749033670650, 0.867400376206203, 0.869939400546865, 0.872370505559332, 0.874697978675220,
            0.876925990271000, 0.879058589396044, 0.881099700704639, 0.883053122456010, 0.884922525455174, 0.886711452816534, 0.888423320441309, 0.890061418108991,
            0.891628911091928, 0.893128842210697, 0.894564134256109, 0.895937592711435, 0.897251908715665, 0.898509662215354, 0.899713325258851, 0.900865265392389,
            0.901967749122769, 0.903022945416089, 0.904032929206259, 0.904999684890892, 0.905925109795622, 0.906811017590966, 0.907659141648569, 0.908471138326099,
            0.909248590172164, 0.909993009044478, 0.910705839136137, 0.911388459906251, 0.912042188912396, 0.912668284543375, 0.913267948651688, 0.913842329085823,
            0.914392522123137, 0.914919574804594, 0.915424487173083, 0.915908214417344, 0.916371668923867, 0.916815722239275, 0.917241206945921, 0.917648918453527,
            0.918039616709745, 0.918414027832623, 0.918772845667907, 0.919116733274176, 0.919446324338706, 0.919762224526993, 0.920065012768752, 0.920355242483164,
            0.920633442746096, 0.920900119401882, 0.921155756122228, 0.921400815414685, 0.921635739583042, 0.921860951641913, 0.922076856187712, 0.922283840228078,
            0.922482273971780, 0.922672511580987, 0.922854891887758, 0.923029739076469, 0.923197363333861, 0.923358061468284, 0.923512117499646, 0.923659803221505,
            0.923801378736669, 0.923937092967607, 0.924067184142903, 0.924191880260922, 0.924311399531813, 0.924425950798903, 0.924535733940487, 0.924640940252971,
            0.924741752816278, 0.924838346842377, 0.924930890007754, 0.925019542770606, 0.925104458673489, 0.925185784632135, 0.925263661211099, 0.925338222886867,
            0.925409598299047, 0.925477910490190, 0.925543277134818, 0.925605810758166, 0.925665618945134, 0.925722804539933, 0.925777465836869, 0.925829696762711,
            0.925879587051042, 0.925927222409001, 0.925972684676794, 0.926016051980333, 0.926057398877367, 0.926096796497422, 0.926134312675897, 0.926170012082609,
            0.926203956345105, 0.926236204167021, 0.926266811441777, 0.926295831361888, 0.926323314524139, 0.926349309030908, 0.926373860587863, 0.926397012598303,
            0.926418806254367, 0.926439280625341, 0.926458472743321, 0.926476417686408, 0.926493148659705, 0.926508697074282, 0.926523092624360, 0.926536363362882,
            0.926548535775687, 0.926559634854486, 0.926569684168800, 0.926578705937067, 0.926586721097076, 0.926593749375894, 0.926599809359446, 0.926604918561886,
            0.926609093494906, 0.926612349737095, 0.926614702003460, 0.926616164215220, 0.926616749569925, 0.926616470611985, 0.926615339303643, 0.926613367096421,
            0.926610565003025, 0.926606943669707, 0.926602513449013, 0.926597284472866, 0.926591266725860, 0.926584470118642, 0.926576904561233, 0.926568580036068,
            0.926559506670566, 0.926549694808948, 0.926539155083043, 0.926527898481755, 0.926515936418866, 0.926503280798798, 0.926489944079956, 0.926475939335249,
            0.926461280309352, 0.926445981472300, 0.926430058068958, 0.926413526163938, 0.926396402681524, 0.926378705440199, 0.926360453181351, 0.926341665591814,
            0.926322363319874, 0.926302567984466, 0.926282302177300, 0.926261589457721, 0.926240454340171, 0.926218922274191, 0.926197019616962, 0.926174773598468,
            0.926152212279437, 0.926129364502313, 0.926106259835562, 0.926082928511715, 0.926059401359638, 0.926035709731551, 0.926011885425441, 0.925987960603514,
            0.925963967707436, 0.925939939371126, 0.925915908331899, 0.925891907340796, 0.925867969072934, 0.925844126038713, 0.925820410496707, 0.925796854369056,
            0.925773489160105, 0.925750345879049, 0.925727454967237, 0.925704846230762, 0.925682548778891, 0.925660590968801, 0.925639000357026, 0.925617803657947,
            0.925597026709546, 0.925576694446602, 0.925556830881405, 0.925537459091991, 0.925518601217833, 0.925500278462859, 0.925482511105589, 0.925465318516156,
            0.925448719179887, 0.925432730727121, 0.925417369968881, 0.925402652937982, 0.925388594935167, 0.925375210579812, 0.925362513864769, 0.925350518214888,
            0.925339236548780, 0.925328681343376, 0.925318864700871, 0.925309798417649, 0.925301494054791, 0.925293963009820, 0.925287216589348, 0.925281266082301,
            0.925276122833449, 0.925271798316989, 0.925268304209942, 0.925265652465173, 0.925263855383846, 0.925262925687174, 0.925262876587329, 0.925263721857399,
            0.925265475900317, 0.925268153816676, 0.925271771471390, 0.925276345559140, 0.925281893668595, 0.925288434345361, 0.925295987153667, 0.925304572736756,
            0.925314212875979, 0.925324930548585, 0.925336749984194, 0.925349696719926, 0.925363797654187, 0.925379081099061, 0.925395576831277, 0.925413316141699,
            0.925432331883282, 0.925452658517396, 0.925474332158441, 0.925497390616623, 0.925521873438771, 0.925547821947024, 0.925575279275214, 0.925604290402751,
            0.925634902185759, 0.925667163385231, 0.925701124691898, 0.925736838747504, 0.925774360162150, 0.925813745527316, 0.925855053424165, 0.925898344426669,
            0.925943681099109, 0.925991127987409, 0.926040751603788, 0.926092620404133, 0.926146804757498, 0.926203376907081, 0.926262410922015, 0.926323982639268,
            0.926388169594930, 0.926455050944145, 0.926524707368921, 0.926597220973029, 0.926672675163222, 0.926751154515967, 0.926832744628915, 0.926917531956332,
            0.927005603627735, 0.927097047249029, 0.927191950685454, 0.927290401825723, 0.927392488326821, 0.927498297338956, 0.927607915210345, 0.927721427171542,
            0.927838916999198, 0.927960466659307, 0.928086155930104, 0.928216062005031, 0.928350259076359, 0.928488817900291, 0.928631805344612, 0.928779283920221,
            0.928931311298155, 0.929087939813988, 0.929249215961837, 0.929415179880455, 0.929585864834265, 0.929761296692469, 0.929941493409702, 0.930126464511980,
            0.930316210592015, 0.930510722818193, 0.930709982461809, 0.930913960447294, 0.931122616930395, 0.931335900909337, 0.931553749874091, 0.931776089498849,
            0.932002833382759, 0.932233882843799, 0.932469126770498, 0.932708441535872, 0.932951690977574, 0.933198726447813, 0.933449386936036, 0.933703499266739,
            0.933960878374116, 0.934221327654469, 0.934484639396505, 0.934750595288788, 0.935018967002745, 0.935289516848700, 0.935561998501526, 0.935836157791607,
            0.936111733555955, 0.936388458543506, 0.936666060367883, 0.936944262500242, 0.937222785294256, 0.937501347034803, 0.937779665001600, 0.938057456538774,
            0.938334440121280, 0.938610336409091, 0.938884869280254, 0.939157766834212, 0.939428762357163, 0.939697595241783, 0.939964011854255, 0.940227766342233,
            0.940488621378183, 0.940746348833358, 0.941000730378567, 0.941251558008764, 0.941498634489439, 0.941741773723655, 0.941980801039472, 0.942215553398326,
            0.942445879525733, 0.942671639966395, 0.942892707066459, 0.943108964886256, 0.943320309047355, 0.943526646518165, 0.943727895342692, 0.943923984317265,
            0.944114852620250, 0.944300449399866, 0.944480733325210, 0.944655672105601, 0.944825241983214, 0.944989427203850, 0.945148219470468, 0.945301617383889,
            0.945449625874797, 0.945592255630878, 0.945729522522640, 0.945861447031121, 0.945988053680372, 0.946109370477287, 0.946225428361010, 0.946336260663863,
            0.946441902585392, 0.946542390680901, 0.946637762365499, 0.946728055434493, 0.946813307600678, 0.946893556048873, 0.946968837007867, 0.947039185339724,
            0.947104634146279, 0.947165214392486, 0.947220954546172, 0.947271880233639, 0.947318013910474, 0.947359374546859, 0.947395977326598, 0.947427833359057,
            0.947454949403170, 0.947477327602643, 0.947494965231484, 0.947507854448987, 0.947515982063309, 0.947519329302787, 0.947517871594179, 0.947511578347044,
            0.947500412743488, 0.947484331532592, 0.947463284828834, 0.947437215913904, 0.947406061041358, 0.947369749243609, 0.947328202140827, 0.947281333751388,
            0.947229050303576, 0.947171250048329, 0.947107823072892, 0.947038651115337, 0.946963607379979, 0.946882556353837, 0.946795353624372, 0.946701845698837,
            0.946601869825707, 0.946495253818749, 0.946381815884426, 0.946261364453484, 0.946133698017658, 0.945998604972637, 0.945855863468558, 0.945705241269449,
            0.945546495623253, 0.945379373144211, 0.945203609709602, 0.945018930373028, 0.944825049296638, 0.944621669704924, 0.944408483862925, 0.944185173081950,
            0.943951407756119, 0.943706847433332, 0.943451140924479, 0.943183926454973, 0.942904831862965, 0.942613474848798, 0.942309463280546, 0.941992395560675,
            0.941661861059106, 0.941317440618113, 0.940958707134674, 0.940585226226009, 0.940196556984108, 0.939792252825085, 0.939371862439159, 0.938934930846951,
            0.938481000567584, 0.938009612903819, 0.937520309349024, 0.937012633120309, 0.936486130821498, 0.935940354238842, 0.935374862271459, 0.934789222997403,
            0.934183015875007, 0.933555834077752, 0.932907286959309, 0.932237002643631, 0.931544630733076, 0.930829845125426, 0.930092346928458, 0.929331867458358,
            0.928548171305831, 0.927741059451235, 0.926910372407530, 0.926055993367286, 0.925177851327568, 0.924275924164136, 0.923350241624265, 0.922400888205585,
            0.921428005886725, 0.920431796674345, 0.919412524930373, 0.918370519443000, 0.917306175205280, 0.916219954866127, 0.915112389820036, 0.913984080904141,
            0.912835698674098, 0.911667983233946, 0.910481743599323, 0.909277856578282, 0.908057265159402, 0.906820976402731, 0.905570058835370, 0.904305639360000,
            0.903028899691297, 0.901741072341771, 0.900443436185074, 0.899137311630983, 0.897824055452023, 0.896505055306898, 0.895181724010405, 0.893855493603252,
            0.892527809278065, 0.891200123219824, 0.889873888419917, 0.888550552523045, 0.887231551765191, 0.885918305058996, 0.884612208280079, 0.883314628804290,
            0.882026900341566, 0.880750318107216, 0.879486134366095, 0.878235554379434, 0.876999732778162, 0.875779770380548, 0.874576711465972, 0.873391541510802,
            0.872225185386694, 0.871078506016354, 0.869952303476905, 0.868847314536535, 0.867764212606216, 0.866703608084845, 0.865666049073358, 0.864652022431047,
            0.863661955145598, 0.862696215987137, 0.861755117415888, 0.860838917712739, 0.859947823302255, 0.859081991238158, 0.858241531822203, 0.857426511328534,
            0.856636954806972, 0.855872848940264, 0.855134144932057, 0.854420761404130, 0.853732587283320, 0.853069484660463, 0.852431291605554, 0.851817824925200,
            0.851228882850232, 0.850664247643069, 0.850123688116087, 0.849606962053744, 0.849113818532680, 0.848644000135296, 0.848197245053523, 0.847773289080552,
            0.847371867489254, 0.846992716796867, 0.846635576416221, 0.846300190194411, 0.845986307840333, 0.845693686242888, 0.845422090682018, 0.845171295934951,
            0.844941087280196, 0.844731261401933, 0.844541627197457, 0.844372006490322, 0.844222234651735, 0.844092161132653, 0.843981649908856, 0.843890579841095,
            0.843818844952178, 0.843766354622662, 0.843733033706510, 0.843718822567865, 0.843723677039781, 0.843747568305524, 0.843790482702754, 0.843852421450665,
            0.843933400299914, 0.844033449104940, 0.844152611318062, 0.844290943404599, 0.844448514178055, 0.844625404054371, 0.844821704224107, 0.845037515741453,
            0.845272948528953, 0.845528120296938, 0.845803155376797, 0.846098183467426, 0.846413338294465, 0.846748756182325, 0.847104574539386, 0.847480930257314,
            0.847877958026009, 0.848295788566398, 0.848734546784070, 0.849194349847585, 0.849675305196256, 0.850177508483232, 0.850701041460791, 0.851245969815953,
            0.851812340965737, 0.852400181822667, 0.853009496542458, 0.853640264267117, 0.854292436878043, 0.854965936774994, 0.855660654698033, 0.856376447610743,
            0.857113136664049, 0.857870505260923, 0.858648297242976, 0.859446215220518, 0.860263919067944, 0.861101024606404, 0.861957102495398, 0.862831677354445,
            0.863724227135001, 0.864634182761574, 0.865560928059321, 0.866503799983403, 0.867462089163009, 0.868435040770189, 0.869421855720600, 0.870421692209869,
            0.871433667585669, 0.872456860551739, 0.873490313696104, 0.874533036331679, 0.875584007633335, 0.876642180051522, 0.877706482978636, 0.878775826640713,
            0.879849106183671, 0.880925205920391, 0.882003003702405, 0.883081375378009, 0.884159199297144, 0.885235360822603, 0.886308756806882, 0.887378299994433,
            0.888442923310119, 0.889501583996312, 0.890553267563308, 0.891596991520441, 0.892631808858505, 0.893656811257642, 0.894671131998757, 0.895673948560643,
            0.896664484889253, 0.897642013329851, 0.898605856217098, 0.899555387122252, 0.900490031760708, 0.901409268566799, 0.902312628946283, 0.903199697219989,
            0.904070110274843, 0.904923556940810, 0.905759777114180, 0.906578560649095, 0.907379746040314, 0.908163218920834, 0.908928910398319, 0.909676795254211,
            0.910406890029029, 0.911119251016722, 0.911813972190031, 0.912491183077737, 0.913151046613396, 0.913793756973787, 0.914419537423805, 0.915028638182989,
            0.915621334327329, 0.916197923738388, 0.916758725110254, 0.917304076023323, 0.917834331092445, 0.918349860195623, 0.918851046788129, 0.919338286305709,
            0.919811984659460, 0.920272556823899, 0.920720425518905, 0.921156019985329, 0.921579774853403, 0.921992129102410, 0.922393525109571, 0.922784407785614,
            0.923165223794113, 0.923536420851390, 0.923898447103490, 0.924251750576557, 0.924596778696793, 0.924933977876077, 0.925263793159257, 0.925586667929099,
            0.925903043664877, 0.926213359750598, 0.926518053328911, 0.926817559196781, 0.927112309739100, 0.927402734896452, 0.927689262163344, 0.927972316613289,
            0.928252320947197, 0.928529695561626, 0.928804858633485, 0.929078226217899, 0.929350212355943, 0.929621229189066, 0.929891687077038, 0.930161994716295,
            0.930432559255603, 0.930703786405965, 0.930976080541713, 0.931249844789752, 0.931525481103881, 0.931803390321152, 0.932083972197188, 0.932367625417362,
            0.932654747580739, 0.932945735153632, 0.933240983389639, 0.933540886212958, 0.933845836061821, 0.934156223688830, 0.934472437915020, 0.934794865334466,
            0.935123889966292, 0.935459892851015, 0.935803251588187, 0.936154339812463, 0.936513526605321, 0.936881175839853, 0.937257645456262, 0.937643286665981,
            0.938038443082616, 0.938443449778329, 0.938858632264683, 0.939284305397502, 0.939720772205850, 0.940168322645892, 0.940627232281148, 0.941097760891412,
            0.941580151013550, 0.942074626418325, 0.942581390528474, 0.943100624784370, 0.943632486964825, 0.944177109471844, 0.944734597589478, 0.945305027728271,
            0.945888445668210, 0.946484864814490, 0.947094264481798, 0.947716588224166, 0.948351742228766, 0.948999593793202, 0.949659969906924, 0.950332655958308,
            0.951017394589658, 0.951713884722833, 0.952421780778413, 0.953140692111180, 0.953870182684187, 0.954609771002868, 0.955358930329273, 0.956117089194870,
            0.956883632228105, 0.957657901310295, 0.958439197070331, 0.959226780725089, 0.960019876268539, 0.960817673008149, 0.961619328442589, 0.962423971469814,
            0.963230705909542, 0.964038614319047, 0.964846762076045, 0.965654201697562, 0.966459977358927, 0.967263129572809, 0.968062699984362, 0.968857736235401,
            0.969647296848079, 0.970430456076838, 0.971206308676690, 0.971973974535983, 0.972732603122932, 0.973481377697248, 0.974219519241179, 0.974946290068116,
            0.975660997071595, 0.976362994582855, 0.977051686811041, 0.977726529846481, 0.978387033214126, 0.979032760971008, 0.979663332348276, 0.980278421944982,
            0.980877759486947, 0.981461129169845, 0.982028368610800, 0.982579367437308, 0.983114065546072, 0.983632451067303, 0.984134558072218, 0.984620464062837,
            0.985090287283704, 0.985544183895044, 0.985982345045952, 0.986404993884758, 0.986812382541708, 0.987204789116647, 0.987582514701582, 0.987945880464997,
            0.988295224821502, 0.988630900707182, 0.988953272977632, 0.989262715942488, 0.989559611047094, 0.989844344709038, 0.990117306314518, 0.990378886377035,
            0.990629474858635, 0.990869459651948, 0.991099225219600, 0.991319151386075, 0.991529612275969, 0.991730975391615, 0.991923600822329, 0.992107840577049,
            0.992284038031788, 0.992452527483164, 0.992613633799276, 0.992767672159260, 0.992914947873107, 0.993055756273571, 0.993190382672375, 0.993319102373318,
            0.993442180735309, 0.993559873278835, 0.993672425829810, 0.993780074695266, 0.993883046865775, 0.993981560239973, 0.994075823866994, 0.994166038203046,
            0.994252395378751, 0.994335079474267, 0.994414266799535, 0.994490126177352, 0.994562819227238, 0.994632500648355, 0.994699318499988, 0.994763414478318,
            0.994824924188415, 0.994883977410568, 0.994940698360227, 0.994995205940975, 0.995047613990076, 0.995098031516246, 0.995146562929398, 0.995193308262189,
            0.995238363383276, 0.995281820202228, 0.995323766866111, 0.995364287947797, 0.995403464626070, 0.995441374857656, 0.995478093541292, 0.995513692673987,
            0.995548241499637, 0.995581806650155, 0.995614452279276, 0.995646240189228, 0.995677229950417, 0.995707479014300, 0.995737042819607, 0.995765974892062,
            0.995794326937751, 0.995822148930275, 0.995849489191823, 0.995876394468276, 0.995902909998464, 0.995929079577685, 0.995954945615566, 0.995980549188375,
            0.996005930085861, 0.996031126852698, 0.996056176824612, 0.996081116159266, 0.996105979861970, 0.996130801806291, 0.996155614749645, 0.996180450343934,
            0.996205339141345, 0.996230310595376, 0.996255393057221, 0.996280613767627, 0.996305998844369, 0.996331573265495, 0.996357360848535, 0.996383384225874,
            0.996409664816520, 0.996436222794546, 0.996463077054490, 0.996490245174051, 0.996517743374452, 0.996545586478866, 0.996573787869360, 0.996602359442831,
            0.996631311566462, 0.996660653033250, 0.996690391018212, 0.996720531035881, 0.996751076899770, 0.996782030684473, 0.996813392691117, 0.996845161416883,
            0.996877333529322, 0.996909903846187, 0.996942865321504, 0.996976209038576, 0.997009924210576, 0.997043998189359, 0.997078416483056, 0.997113162782960};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);
        getSupportActionBar().setTitle("Cane Control");

        textv = (TextView) findViewById(R.id.textView);
        textv.setMovementMethod(new ScrollingMovementMethod());
        textBatt = (TextView) findViewById(R.id.textLevel);
        btn_connect = (Button) findViewById(R.id.buttonConnect);
        btn_send = (Button) findViewById(R.id.buttonSend);
        btn_debug = (Button) findViewById(R.id.buttonDebug);
        radioG = (RadioGroup) findViewById(R.id.sys_group);
        radioF = (RadioGroup) findViewById(R.id.frec_group);
        radioFSM = (RadioButton) findViewById(R.id.FSM_radio_btn);
        radioNN = (RadioButton) findViewById(R.id.MLP_radio_btn);
        radioHF = (RadioButton) findViewById(R.id.HF_radio_btn);
        radioLF = (RadioButton) findViewById(R.id.LF_radio_btn);
        batLevelBar = (ProgressBar) findViewById(R.id.batteryLevel);
        batLevelBar.setMax(100);
        textBatt.setText("0%");
        btn_send.setEnabled(false);
        btn_debug.setEnabled(false);
        stateMachine = new Fsm(1.0f/35);

        dir = new File(fullPath);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        service_init();

        // Handle Disconnect & Connect button
        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    if (btn_connect.getText().equals("Connect")){
                        btn_connect.setText("Connecting...");
                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    } else {
                        //Disconnect button pressed
                        if (mDevice!=null)
                        {
                            mService.disconnect();
                            battService.disconnect();
                        }
                    }
                }
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String strFreq, strSys, strFile;
                byte[] value = new byte[2];

                if (validate()) {
                    if(radioFSM.isChecked()){
                        system_sel = 0; //FSM seleccionado
                        strSys = "_FSM";
                        if(radioHF.isChecked()){
                            freq_sel = 1;
                            strFreq = "_HighFreq_";
                        } else {
                            freq_sel = 0;
                            strFreq = "_LowFreq_";
                        }
                    } else {
                        system_sel = 1; //MLP seleccionado
                        strSys = "_MLP";
                        if(radioHF.isChecked()){
                            freq_sel = 1;
                            strFreq = "_HighFreq_";
                            stateMachine.setTickerInsert(1.0f/200);
                        } else {
                            freq_sel = 0;
                            strFreq = "_LowFreq_";
                            stateMachine.setTickerInsert(1.0f/100);
                        }
                    }





                    strFile = dateStr.format(new Date()) + strSys + strFreq + "\n";

                    try {
                        Date fDate = new Date();
                        fName_sensor = dateFile.format(fDate) + strSys + strFreq + "_sensor.txt";
                        fName_bat = dateFile.format(fDate) + strSys + strFreq + "_bat.txt";
                        file_sensor = new File(fullPath, fName_sensor);
                        file_bat = new File (fullPath, fName_bat);
                        file_sensor.createNewFile();
                        file_bat.createNewFile();

                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    value[0] = freq_sel;
                    value[1] = system_sel;
                    mService.writeRXCharacteristic(value);
                    textv.append(strFile);
                    try {
                        FileOutputStream fOut = new FileOutputStream(file_sensor, true);
                        fOut.write(strFile.getBytes());
                        fOut.close();
                    } catch(IOException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                    }
                    radioF.clearCheck();
                    radioG.clearCheck();
                }
            }
        });

        /*btn_debug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String strDebug = "#";
                    byte[] value;
                    if(!debug){
                        textv.setText(null);
                    }
                    debug = true;
                    value = strDebug.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });*/
    }

    private boolean validate() {
        boolean validFreq, validSystem;

        if(!radioFSM.isChecked() && !radioNN.isChecked()){
            radioNN.setError("Selec Item");
            validSystem = false;
        } else {
            radioNN.setError(null);
            validSystem = true;
        }
        clearFocus();

        if(!radioHF.isChecked() && !radioLF.isChecked()){
            radioHF.setError("Selec Item");
            validFreq = false;
        } else {
            radioLF.setError(null);
            validFreq = true;
        }
        clearFocus();

        if(validFreq && validSystem){
            return true;
        } else {
            return false;
        }
    }

    private static void toggleTextInputLayoutError(@NonNull TextInputLayout textInputLayout,
                                                   String msg) {
        textInputLayout.setError(msg);
        if (msg == null) {
            textInputLayout.setErrorEnabled(false);
        } else {
            textInputLayout.setErrorEnabled(true);
        }
    }

    private void clearFocus() {
        View view = this.getCurrentFocus();
        if (view != null && view instanceof EditText) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context
                    .INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

    private ServiceConnection BattServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            battService = ((BatteryService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + battService);
            if (!battService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            battService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        //Handler events that received from UART service
        public void handleMessage(Message msg) {

        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            final String strData;
            final Intent mIntent = intent;
            //*********************//

            if (action.equals(UartService.ACTION_GATT_CONNECTED_UART)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String strLog = dateStr.format(new Date()) + " Connected to BLE\n";
                        try{
                            FileOutputStream fOut = new FileOutputStream(file_sensor, true);
                            fOut.write(strLog.getBytes());
                            fOut.close();
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                        textv.append(strLog);
                        Log.d(TAG, "UART_CONNECT_MSG");
                        btn_connect.setText("Disconnect");
                        mState = UART_PROFILE_CONNECTED;
                        btn_send.setEnabled(true);
                        btn_debug.setEnabled(true);
                    }
                });
            }

            if (action.equals(UartService.ACTION_GATT_DISCONNECTED_UART)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String strLog = dateStr.format(new Date()) + " Disconnected to BLE\n";
                        try{
                            FileOutputStream fOut = new FileOutputStream(file_sensor, true);
                            fOut.write(strLog.getBytes());
                            fOut.close();
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                        textv.append(strLog);
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        btn_connect.setText("Connect");
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        btn_send.setEnabled(false);
                        btn_debug.setEnabled(false);
                    }
                });
            }

            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED_UART)) {
                mService.enableTXNotification();
            }

            if (action.equals(UartService.ACTION_DATA_AVAILABLE_UART)) {
                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA_UART);
                byte controlE = txValue[0];
                Log.d("Cane", "Data avaible" + txValue.length);
                if((controlE == 0) && (system_sel == 0)) {
                    counter++;
                    final float medS = ByteBuffer.wrap(Arrays.copyOfRange(txValue, 1, 3)).getShort() / 1000.0f;
                    final float medNS = ByteBuffer.wrap(Arrays.copyOfRange(txValue, 3, 5)).getShort() / 1000.0f;
                    char varS = ByteBuffer.wrap(Arrays.copyOfRange(txValue,5, 7)).getChar();
                    char varNS = ByteBuffer.wrap(Arrays.copyOfRange(txValue, 7,9)).getChar();

                    float varS_f = varS / 100.0f;
                    float varNS_f = varNS / 100.0f;
                    double sdS = Math.floor(sqrt(varS_f) * 1000) / 1000;
                    double sdNS = Math.floor(sqrt(varNS_f) * 1000) / 1000;

                    strData = dateStr.format(new Date()) + " Activity " + counter + "\n" + "Step mean: " + medS + " s\n" + "NoStep mean: "
                            + medNS + " s\n" + "Step standard deviation: " + sdS + " ms\n" + "NoStep standard deviation: " + sdNS + " ms\n\n";
                    textv.append(strData);
                    try {
                        FileOutputStream fOut = new FileOutputStream(file_sensor, true);
                        fOut.write(strData.getBytes());
                        fOut.close();
                    } catch(IOException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                    }

                } else if ((controlE == 1) && (system_sel == 1)) {
                    for(int i = 1; i <= 18; i=i+2){
                        Log.d("Cane","num: " + i + " " + ByteBuffer.wrap(Arrays.copyOfRange(txValue, i, i+2)).getShort());
                        stateMachine.stateMachine(nnCompute(ByteBuffer.wrap(Arrays.copyOfRange(txValue, i, i+2)).getShort()));
                    }
                    if(stateMachine.numActivitiesNoPrinted() > 0) {
                        strData = stateMachine.getActivity();
                        textv.append(strData);
                        try {
                            FileOutputStream fOut = new FileOutputStream(file_sensor, true);
                            fOut.write(strData.getBytes());
                            fOut.close();
                        } catch(IOException e) {
                            Log.e("Exception", "File write failed: " + e.toString());
                        }
                    }
                }
            }

            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }
        }
    };

    private boolean nnCompute (short input){
        if(input < 0){
            input = 0;
        } else if (input > 1023){
            input = 1023;
        }

        return (nnArray[input] > 0.5);
    }

    private final BroadcastReceiver BatteryStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            final String strBatt;
            final Intent mIntent = intent;
            //*********************//

            if (action.equals(BatteryService.ACTION_GATT_CONNECTED_BATT)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "BATT_CONNECT_MSG");
                        btn_connect.setText("Disconnect");
                        //mState = UART_PROFILE_CONNECTED;
                        btn_send.setEnabled(true);
                    }
                });
            }

            if (action.equals(BatteryService.ACTION_GATT_DISCONNECTED_BATT)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        btn_connect.setText("Connect");
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        btn_send.setEnabled(false);
                    }
                });
            }

            if (action.equals(BatteryService.ACTION_GATT_SERVICES_DISCOVERED_BATT)) {
                battService.enableBatteryNotification();
            }

            if (action.equals(BatteryService.ACTION_DATA_AVAILABLE_BATT)) {
                final String battPer;
                final byte[] txBatValue = intent.getByteArrayExtra(BatteryService.EXTRA_DATA_BATT);
                final int batValue = map(txBatValue[0],0, 127, 0, 100);
                strBatt = dateStr.format(new Date()) + " Battery level: " + batValue + "%\n";
                /*if(batValue == 0){
                    strBatt = "Low Battery Level. Disconeccting...\n";
                    textv.setText(strBatt);
                    mService.disconnect();
                    battService.disconnect();
                } else {
                    strBatt = dateStr.format(new Date()) + " Battery level: " + batValue + "%\n";
                }*/

                battPer = batValue + "%";
                if(batValue > 0) {
                    batLevelBar.setProgress(batValue);
                    textBatt.setText(battPer);
                } else {
                    batLevelBar.setProgress(0);
                    textBatt.setText("0%");
                }

                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            FileOutputStream fOut = new FileOutputStream(file_bat, true);
                            fOut.write(strBatt.getBytes());
                            fOut.close();
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });

            }

            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }
        }
    };

    private int map(int x, int in_min, int in_max, int out_min, int out_max) {
        int temp = (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
        if (temp < out_min){
            return out_min;
        } else if(temp > out_max){
            return out_max;
        } else {
            return temp;
        }
    }

    private void service_init() {
        Intent UARTIntent = new Intent(this, UartService.class);
        bindService(UARTIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
        Intent BattIntent = new Intent(this, BatteryService.class);
        bindService(BattIntent, BattServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(BatteryStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED_UART);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED_UART);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED_UART);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE_UART);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        intentFilter.addAction(BatteryService.ACTION_GATT_CONNECTED_BATT);
        intentFilter.addAction(BatteryService.ACTION_GATT_DISCONNECTED_BATT);
        intentFilter.addAction(BatteryService.ACTION_GATT_SERVICES_DISCOVERED_BATT);
        intentFilter.addAction(BatteryService.ACTION_DATA_AVAILABLE_BATT);
        intentFilter.addAction(BatteryService.DEVICE_DOES_NOT_SUPPORT_BATT);
        return intentFilter;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService = null;
        battService.stopSelf();
        battService = null;
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    //((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                    mService.connect(deviceAddress);
                    battService.connect(deviceAddress);
                    Toast.makeText(this, "Connect to BLE Cane", Toast.LENGTH_SHORT).show();
                } else {
                    btn_connect.setText("Connect");
                    Toast.makeText(this, "Imposible to connect. Try again", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
        }
    }

}
