package fr.poulpogaz.animescandl.scan.japscan;

import fr.poulpogaz.animescandl.model.Chapter;
import fr.poulpogaz.animescandl.scan.iterators.PageIterator;
import fr.poulpogaz.animescandl.utils.Pair;
import fr.poulpogaz.animescandl.utils.log.ASDLLogger;
import fr.poulpogaz.animescandl.utils.log.Loggers;
import fr.poulpogaz.animescandl.website.WebsiteException;
import fr.poulpogaz.json.JsonException;
import fr.poulpogaz.json.tree.JsonArray;
import fr.poulpogaz.json.tree.JsonObject;
import fr.poulpogaz.json.tree.JsonTreeReader;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.*;

public abstract class BaseIterator<T> implements PageIterator<T> {

    private static final String MAGIC_STRING_1 = "AzFpUnL5uICH63DtSVmv1x4P7b9Z8y0idcrGhQJNgTelK2BMEWRfYjowOakqXs";
    private static final String MAGIC_STRING_2 = "wASN0luxq3k1DYm69gV8FIQPb7EhtjBfoHLOMaGCsRicdTp5nUezWJr2ZKy4vX";

    private static final ASDLLogger LOGGER = Loggers.getLogger(BaseIterator.class);

    protected final Japscan japscan;
    protected final Chapter chapter;

    protected final JsonArray pages;
    protected int index = 0;

    protected final Map<Character, Character> map = new HashMap<>();

    public BaseIterator(Japscan japscan, Chapter chapter) throws IOException, InterruptedException, JsonException {
        this.japscan = japscan;
        this.chapter = chapter;
        pages = loadPages();
    }

    @Override
    public boolean hasNext() {
        return index < pages.size();
    }

    protected String nextURL() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        return pages.get(index).getAsString();
    }

    protected JsonArray loadPages() throws IOException, InterruptedException, JsonException {
        Document document = japscan.getDocument(chapter.getUrl());
        Element data = document.selectFirst("i#data");

        JsonObject json = decode(data.attr("data-data"));

        return json.getAsArray("imagesLink");
    }

    private JsonObject decode(String encodedJson) throws JsonException, IOException {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < encodedJson.length(); i++) {
            char c = encodedJson.charAt(i);

            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                sb.append(MAGIC_STRING_1.charAt(MAGIC_STRING_2.indexOf(c)));
            } else {
                sb.append(c);
            }
        }

        byte[] base64Decoded = Base64.getDecoder().decode(sb.toString());

        return (JsonObject) JsonTreeReader.read(new ByteArrayInputStream(base64Decoded));
    }

    /*protected String getJS(Document chapterDocument) throws IOException, InterruptedException {
        Element element = chapterDocument.selectFirst("script[src^=/zjs/]");
        String jsUrl = element.attr("src");

        try (InputStream is = japscan.getInputStream(jsUrl)) {
            return new String(is.readAllBytes());
        }
    }*/

    @Override
    public Optional<Integer> nPages() {
        return Optional.of(pages.size());
    }

    public static void main(String[] args) {
        String str = "ijGqoUFfhv3fZJnX3lgjK93D3c287OgJo1gjh9Fc7fFXKUoqhg28KTSlh9Fq7jF4KveAovGx7T6FHTMMuUxNuUxMHVcBWU6FoOgHuASHuj3X3VFM7VoM0TIFhj3D3V6MhT067Vt6KVcBHYgjiUELovCrW9FqK9FqWvGNoOSLovec3f2fKUFMhTgAROcqKj3DUjGro1e2HANHuF28WTequlCBWveNWTSX71yqKUEHuTc6hF28Wjx4WvpAWTSquloAvkEJ5UnB5J5Y5RsxWAef5JwB5JsA5UW25Aaj5VaBhRHI5RsFCmSy5JWIZR525RaBWAaF5TaIWR0x5R0jZRaB5VaI5mnB5UnQZmnx5A3B5myx5UaIZmax5JHIhmSVCm3ACJnIZRHIWJwF5R5AWRSMZR0I5RcJ5R0I5ASVZmWIhmcHuTWICJw2CmWx5Aa2ZR5Bhm3QCO0AZmSJZmwIWJ3Y5U02hJn27OnICR5ICO52Cm0B5A5FCA3xCRWxZRef7RnB5RgyCO3Y5AaB5R5F5Uxy5RWFWAa2ZOnFWTcVCme6CAwTZU3IZmsx5RgqCmwYZRyIWVQB5maAZmGf5J0ACJCy5F28WVrx5V5xWR3j5JHAZmoJ5V325m5jZR5jhVBA5JGXhmpcCAnjZOBB5VeXCJwxZRwjCmhJ5J0IhmCM5AyACUrx5Jeqhm32CAaAWRwQ5AyA5J525RyAhm3Q5AyYCAGyKUWj5V2T5mSqCAGf7R0j5TIM5OnACR3ICU5jCRyA5Vh4vktT5VeqhJwqKlpl3f2fK1eBH15DvkEHuTCy7fxAoOSBKUCM7OIxuVc8vkEN7UoHuT5qKVS2HTCM7fxYHF285Acc5JnIhRcfCA3jCJwT5JwT5AyQZmwj5mpJ5JsFCJyT5A3xCApMZU0IWJcM5Vaj5JnAZR0A5Rcy5m5jCmpMZR0xWJcMCV5xWAnx5JsYZmy2ZRwj5JpVZU0xWRGf5U5xCmoMZRnQZmy25R0xWJHBZU5YCmcMZRHxCRWYZUWYvktxZR5QZm3xCAnjCJHI5Js2hRGJ5U5x5JWjZRH2CAcJZmHxhU2AZRsICm3jZOaAhRSV5An25m5QCT0jhOFy5J0AWAGcCUaj5Ayj5ACqCJcM5AWjCJhc5ThNCR3Y7RwQWJMMZU5TCmyY7JHQZRHjZUSqCmsF5Rw25JwT5RnI5mSHuAg45JwACT025Rpc5U3F5JwTZmWI5mHA5mM65mw27O5QZm0T5mh65JwY7OnQhRof5OnBhmwjZR5ICRnI5Ue45AwB7Vn2CRgf5UnQWASJ5RyIZmyQ5Rw25mnjCUW2hUcM5OGXWAsB7Vn25UBQ5mpXCRMf5Ra2WA5B5OnYCAwFKc28CApV7JyQuVN2hj3X3VMBo1pAZc28vkEJhOQqHYeMoOcJWUIXi9xN7F28KUFlvkEJuVNMH1CJWUQqoYCHuT0xhR3Y5RHx5AoV5V32Zm3jCVaxWRsI5Jw2Zm3xCU5x5J5TZR32hRcJ5U3xhJ3I5V3IZmyx5TWxhJwF5JW2CmccZU5x5RWxZUaI5m3xCTax5mcy5J025AyYZRyjZmnBZUaYhJyAZmaxZRnYZRsY5mcJCA3x5JccZRHTZRyjCF285JcfZOWjWRHx5J5YCmGM5m0jWASMZRyThJyj5m0xWRsFZRpX5JcV5R3jhms25AyIWR5x5OnA5AHI5Jc6hJ3B5T0jCJ0T5V3xZmCc7JsxWACc5V5T5R5FKU5jhVBBZmnxCAcfCVnxhUxJZOaY5JyI7J0QWRnT5m52hJnF5R3IvkEfKV02hRoc5mH2CAnACUW2WJsQ5R3YhmpM7Rn25O2QZmyFWAwj7RW25VIcZO5YWJpVCmn2WJyQ5RyIhJSVKJ02CVQB5O0FWAnIZmyIhmn25Ryxhmn25OaICR0A5moNZRwQ7maQhOxV5mC6Cmwx7mHQZmn25O5A5JwjCT02CTNHuAy25VQjZkx4HOHfukGro1e2HANHuF28WTequlCBWveNWTSX71yqKUEHuTc6hF28Wjx4WvpAWTSquloAvkEcZU3j5An2ZR3YZR3I5O0jZRWYZRWQ5RGf5O3j5A0FZU5A5RyF5m0xhJSfZU5jZm3Q5R3xCmCyZUn2CJ3A5m3xhJcVZU3TWRcV5U3j5mHjZU3xCRGJ5OWxWRcf5VnI5myBCTnxCJsxZRnI5JyTCAnx5RHBZRyxCJyBCVWx5RoHuAnxCmMV5VnYCR3xCTWjhJwF5V0IZRyBCJ5xWJwQZRsQCJcM7mHxCJnx5JsQ5J5Y5RyA5mwQ5A0YCR3Q7RHjCA5T5JsFCJ3AZRyAWVxyZR0ACR3FCJaA5OyQ5Jp6CJsY5myx5JWTZRgqCJsICA5xhVQYZO3I5Awj5mWIhmn25g28CVNV5O0YWJwA5mwI5J0x5mnQhRnxCAH2CVBI5OGXWJsACR02WTBI5mgXCRMVCT32WAaQ5msx5JnB5UaIWUrT5mCq5JpVCUWIWJMM5UWIWJn2ZR3IWJwF5U5FhJpMKU32WU22ZmeqWRwT7Ry25U2jZmWI5RwI5Aa2ZRHT5me4vkEV5mSq5RsqKlpl3f2fK1eBH15DvkEHuTCy7fxAoOSBKUCM7OIxuVc8vkEN7UoHuT5qKVS2HTCM7fxYHF28CRMV5Rs25JsTCVaIZRyx5R0FWRsICAaIhmcc5R3BhRsI5JwQCmcVZmW2CmsT5U3IZRwQZmsjWRMfZRyIWRcyZmyQ5mMMCRaQhRwF5RsTZRMcZmWI5JcfZO0QCAnY5OaQWRWYZOWYhJsY5OaQWJWAZO3TWRsFZO0QWAgfZmWTvkt2ZO0YWRSJCJWIWJhc5RWx5RSy5maQZm0IZOnxCRsYCAsQWUIJZm52CAnYCA5jhRwQ5JyxWJ3ACJ0IhUFc5UWj5JSVCmsICmsj5VgqWRsQ5J0I5A0F5VhNCRnF7R5YZmw2ZO3FZRMM7JsYWJhyZmSq5Aof5O5xWAyA5my25mpHuAS4CRyFCVWxZmyj5mWBhmyxCAy2CRhMZRS6Cmcy7O0Y5JecZRS65myB7OWYCJWBZRWAWJyxZms2ZmpM5OG4ZRcM7VaxZRa25OnYCmpc5m02Zmsx5O0xCJwjCO3xhUyYZRoXhmoM7JHxhVFVZRoXhmof5m5xWJGMZR0ThRcJKc28WJyT7JwYuVN2hj3X3VMBo1pAZc28vkEJhOQqHYeMoOcJWUIXi9xN7F28KUFlvkEJuVNMH1CJWUQqoYCHuTWQ5mSV5m3Q5mhy5U3xCmnFCUnQCmoM5UaxWAnTCOaQhmGfZm3xWRsx5OWQCJnx5U32WJsx5JyQZmcf5UWxhJs2Zm0QWA02Zmn2WRn2CV5Q5AMy5RnxhmsIZmwI5mpMZmHThRMfCAHQCmpyZOnTZRsICV3QWRMJZOWFCAMyCc28CJs2CTaI5AW25RWTCAnxZUnI5ApJZm3FWRMfZUnQCAH2ZmoXZRsI5myIhmoV5Jy25R3FZU3j5RhM5Rh6ZRSJ5JwICmaA5UnQCA3A7VnQWJ3I5R0F5m3IKRyICOFfCT3IhRsBCU3Q5OQTCA3T5RsB7VWY5Aw2ZU0xCRpJ5ms2vkEyKJWxhJW2ZUWx5Jw2COaxWJHB5m0TWRyB7UnxhOIJCAWBhmcy7U3x5VIMCAyThJyT5AyxWJMf5my2ZRwQKV3xhVxVZRwBCRpJCAw25RpM5mwQWAwAZUa25ReyZRSN5my27OnYCTxcZUG6CAyB7OaYWRwxZRnjWJcMCV5xhVNHuAHxhOQTCjx4HOHfukGro1e2HANHuF28WTequlCBWveNWTSX71yqKUEHuTc6hF28Wjx4WvpAWTSquloAvktYZm3IhJpVZmwT5AnjZR5IZmgJZm5YWJn2ZU5I5JeMZmyjCAsAZU0QWJwAZOaI5JSM5msQhRGMZm5xhRnIZRwQZRsFZmWFCRsB5OaIZRhMZmnQhJnxZUnQWRsB5R525RMVCVWQZRoVZmn2CJMVCJ3QCRWFZm5QZms2CRWQ5mhHuAnQ5AH25R5T5RSfCVaICAcc5RW2CJsTCU0QhRyQZOnY5RsT7OaQ5Rwj5RnYWA3B5m3j5JyB5JsTCAn27RWIhJ3I5RsBCAnQZm0jhVxcZm3jhRnTCRnjCTyA5Ro65Aoy5JaQCJgfZOhq5RHjCVnQWUxfCA025JcJZRn2WRwQ5S28WUrYZRHT5JcVZUW25maAZR3Y5AwQCV5x5TFfZRpXWAHjCOnxZUBYZRCXhRofCJ5xhm5jZUnQZmpc5On25TNcZRSq5myxCm02CJHF5ma2hmwxZOn25RcV5myBWRcMKRax5T2ICThqhJyx7UWx5U2TCTW25JyF5J0x5AWQZRp4vktTZRpq5RHqKlpl3f2fK1eBH15DvkEHuTCy7fxAoOSBKUCM7OIxuVc8vkEN7UoHuT5qKVS2HTCM7fxYHF28CRSJCmyAWAnTZR0B5m3QCOnQCASV5m3BCJGVCOnY5mnTCUaIWAGc5RWA5mSVCm5BWR5A5RHFZmnQ5JHB5JGM5U5IZmnAZO0IWJCfCmyxWAnY5RnBCAGy5U0ICJaB5AaIhRyY5R32hRnx5AsIZmy25RsxhJSf5UWI5JMf5RyxvktA5Rn25JaAZRyBCmy2Cm3jCJaF5TaI5Jsx5R3jWAnI5O5I5U2x5R5AZma25OaFCA5BCUnjZmgVZR3BhUBBCOnFZReyCA0BZmnBCUCqWJnxCUWBZRsYCRgN5ReJ7U32CJhy5U0QWRnT7Jy25JcV5RoqCmpM5TWj5AGV5T3ACACHuAS45R3FZU5jZRGJ5AWYCR325msAWRyY5Vh65J3T7OW2WRH25Jh6hmGJ7O02CJy25VWT5AGM5R5ACA5Q5AM4CA3j7VnjWAHQ5A52hRCf5T3AZRSJ5AHj5R5YCTajhUyT5JeXCmpc7JHj5VFc5JcXZmwQ5AwjZmgV5V3xhJ3jKc28WJ3x7J52uVN2hj3X3VMBo1pAZc28vkEJhOQqHYeMoOcJWUIXi9xN7F28KUFlvkEJuVNMH1CJWUQqoYCHuAWQCmn25mWQCAhV5RwxhJSfCRaQ5Aoy5UaxWRnTCmWQ5RGcZO0x5RsQ5mWQCJSM5U325mMV5J5QCAcf5RWxWRMVZOWQCR0AZma25JnYCVWQWJsF5UWx5AsIZO0I5RwQZmWT5JMJCA3QhRwQZO0TCmMJCJ3Q5JMJZmyFWRs2Cc28hmMJCAwIZRhM5UWTWJnAZU0I5JpVZOnFhRsxZU5QWJHTZmCXhRsF5mWI5RHT5Va25mGyZU5jWJWA5US6CJnx5JyI5meV5R3QWAGc7VWQWAGJ5U0F5R3jKU5IWUB2CT5BZmsxCRwQCVQFCAyTZmsj7VWYCApcZRHx5Apf5my2vktTKJHxCJWBZRwxCJwYCOWx5moV5O3TCmyA7UaxhOIMCAWBCJyI7U0xhU2ICAsTCJyT5TnxWJs25OW25AwYKV3xCVxMZUWBhJwFCA52Cmwj5mHQhJpfZR02hJaAZRSNhRyI7msYCVQjZUC65Jcc7msYhRwBZU5j5JyjCV5xZUNHuTnx5OxyCjx4HOHfukGro1e2HANHuF28WTequlCBWveNWTSX71yqKUEHuTc6hF28Wjx4WvpAWTSquloAvkEy5myA5mGy5m3QCmCc5UaAWJof5m3xCA5A5UnACRWT5OnBWAw25R52WJ3T5mHAWR5Y5Js25AeJ5OWICm5j5RH2ZRwA5m3Y5Apc5JyAhmsY5m32hm5I5UW2WRwA5AsjWApyZOW25JyI5mnjhRwjZO025msY5m02WJwFCAH2CmMHuT52hmcf5T3QCm5TZmHAZmnx5T5j5RwFCA32CRnY5max5mwj7mn25A3x5T3x5ReM5J3BZRSfCOWQWA5T7UnACAey5TaTCJCy5maB5Txf5mWBhJCMCTWBWVyF5AC65JyjCAn2hmsQ5OhqhJyxZm02WUQYZRHjhJnB5U3jWR3Y5c285VNJ5UaQZRn25Uaj5JWB5U0xhmGfZO3I5VBY5RCXWRyFCJWIWUFJ5RpX5AcfZmWICAgM5RH2Zm3A5V5jhVrB5RGqCAnxCJ5j5JyA5V0jZRGy5mHj5JSy5JnThJSMKR5IZOIJZUeqhJnj7RnI5UIcZR3jhRn2CmHICRMf5Rg4vktY5ReqWAyqKlpl3f2fK1eBH15DvkEHuTCy7fxAoOSBKUCM7OIxuVc8vkEN7UoHuT5qKVS2HTCM7fxYHF28ZRGfCRsBhJGc5msFZm5BCU3x5JGf5R5F5m5FCRyQhJ3BCJyjhR5B5J5BZR3QCU3FhmeV5V0ThJGc5T5FZmCf5JWjWR3jZU5jhmaYCRa2Zm3x5V5FhRCV5Vnj5R0BCm0jhJpc5J0IWJGyCOnjZmpy5J32WA3x5JnjWAyF5J52vktj5JHIhmgy5m3FCmwTCR3AWR0BCOnjCAcV5JHACR325RHjWT2Y5V0BCm0j5RyTZRecCJaAWAhy5msFCTBYCU0T5m0jZm5FZm32CJeqhm3QCJ5FWAyYCJMNCm0Q7UnIhJwj5V32ZRGJ7JWI5mpV5JSqCJSyCmWACR5ICm3BhReHuAg4hR5x5O3AZmCVCO0QZm5Q5RaBWJwj5TG65A527m0IZRMV5Ae6ZRCc7maI5Awx5AsYWJCy5VWBhReVCOg4WA5Q7V3ACJMMCm3IWAeyCmHBCm3QCmyAWAecZOWAWUyI5AeXCRSV7V0ACUFf5TeXWRnQCOaA5JhJ5Ay25RCJKc28CR5A7J3IuVN2hj3X3VMBo1pAZc28vkEJhOQqHYeMoOcJWUIXi9xN7F28KUFlvkEJuVNMH1CJWUQqoYCHuAyAWRWYCU5A5AnBCJHBhmhJ5maAWAGcCV3BCJWxZU3ACmHY5AsBWJCVCU3AhRhcCJ0FCA5YCAHAWJeJCJsBWA5B5T0AWApM5A3F5JWj5RaA5R5TCV3BZmCy5TnTZmgV5AsI5RCf5JHACm0B5TnICA5Y5RyAhmCc5Tn2Cm5B5g28WR5A5J0T5AnTCVWIZmWACmHT5J025T525m5xCmyA5J3j5AhXCJCyCRnT5A3ACT0F5mHjCmHYWRnjCJh6CmhyCA3T5my2CV5ACRHY7JaACAHjCVa25mHTKRsThVBx5JajhRCJ5U3A5VQB5J3IWJ5x7JyjZR0FCOnBWA0ICRHFvktBKJHBZRSMCmHBWA0AZRaBCAGyCU5ICmeJ7U0BWV2T5JyxWJaB7RHBZOIf5VWIWJecZO0BCR5YCRHFCAgfKV5BZOxJCmaxhRgJ5VnF5J0jCR3ACm0ICmWFWJyQCOeNWRaQ7OWjZOQICOG6CAaB7majWR0YCmHYZRey5U0BWUNHuAaBCOxM5fx4HOHfukGro1e2HANHuF28WTequlCBWveNWTSX71yqKUEHuTc6hF28Wjx4WvpAWTSquloAvkEy5mwACm3Y5msQWRCy5RsA5mHB5mnxCJCf5RyA5Rhf5mWBhRwY5Un2CmGJ5m3A5JCc5Jy2hJeJ5O3I5R525Ry25Awj5O3YWRw25VaA5AsY5m02Cm5A5Un2CmwB5TajCmpcZO52WAyY5m5j5mwYZO52WRMJ5OW2CRpVCAa2hmMHuTW2CmyT5AsQZR5QZmyACRSf5AWjCJpVCAa2WASy5O0xZmw27m02CR3x5TnxhmaA5V0B5AnACmsQCRCf7RyACRa25A3ThRCJ5OWBCUQI5O0BCR5BCT0BWUcy5AG6ZRy25mH2hJsI5mCqhRyIZOn25UQ2ZRnj5RnT5R0jCJ3j5c28COrB5UaQ5mnB5RHjZmhM5UaxZm32ZmaI5UBT5UGXZRyICV3ICVFc5RoXWRyBZO5I5J0j5U02ZRGV5V5jWVrI5RpqZRnACJ3jWJcM5Jwjhm3A5mHjCRnB5J5TWRnBKRaIhO2BZUgqWASJ7RHI5T22ZRHjWASMCO5IhJsF5Uh4vktB5UeqhJyqKlpl3f2fK1eBH15DvkEHuTCy7fxAoOSBKUCM7OIxuVc8vkEN7UoHuT5qKVS2HTCM7fxYHF28CAwj5Anj5RpyZmsAhRnB5AnYWRwxZRaA5RSJ5TnThJwICm52WAnF5myj5mwY5AWAZR3x5mnBZRwQ5RyA5mSV5mn25ApyCAa2WJ3I5AyQZmpV5maAZmSy5O025JCM5V325AMJ5mnx5JwQ5Jw2hmsB5OnQ5JwI5mW2CAoM5O5QvkEJ5myxhmCVZm0ACRsx5AWI5mCM5VW2WJoV5OnI5JpcZR02WVIy5O0jCACMZU5BZm3FCm0I5RaFZmyAZUFM5AwB5A5ICVnAhRpMCmpqCJpyCmHAZRoMCmGNCJ5T7RHxZRnY5OaQhJwT7J5xCmMJ5mcqWJcy5V5IWRnx5J0jWRGHuAS4CmnYZm3I5RnI5VaTZmScZRaj5RMM5Rc6CmnI7O5xWRhV5RS65ASJ7m0xZRs25RHFCJnx5myj5R3x5JM4ZRn27VWI5RWA5VaxCmGf5JnjZRwQ5VWICm3QCVaIhUyj5USXWJyA7V5I5UBj5RMX5mcy5JaIWRaj5RnQCJnYKc28WRSJ7V5xuVN2hj3X3VMBo1pAZc28vkEJhOQqHYeMoOcJWUIXi9xN7F28KUFlvkEJuVNMH1CJWUQqoYCHuT3jCm0ACO3jhRwTCRsA5J0QZUWjhRnQCUWACmgyZmWjWAWx5VnAWJ3QCmyj5R0BCRwBZR3ICJwjWJCyCR5AZR3F5Jaj5Jcf5JnBhR0j5O0jCm3jCRWAWR3I5JaFWRaB5J32Cm3T5R0jZReV5J52hm3x5O3jZm3A5Jsx5RGc5S28CA3A5U5FCRpfCRn2WRgf5TnF5AaA5VWxhJGJ5AyjZmnB5JcXWRGyCmnFhJScCJHBCmhM5AnTZmwTCUe6WR0ICJwF5mMJCUWjCJW27JHjZmWxCUaxWAWBKRyFZUBI5RHBhR3F5mwjWVxc5R32CR327J0I5AeJ5AnAWJaQCO5BvktBKV0AWJwB5A5AWJecZmWAZmnjCms2hJCc7RsAhV2B5UaQCJCJ7RyAWUIf5Rs2ZR5ICAaA5m3xCmnB5mefKVaA5TQx5A0QCmaj5U3B5RecCm3jhRaB5A3BCJMM5ASNCmCM7maI5VQB5Ah6CR5x7OWI5Aey5AyTWJ5Q5m5AZONHuAnAhUQY59x4HOHfukGro1e2HANHuF28WTequlCBWveNWTSX71yqKUEHuTc6hF28Wjx4WvpAWTSquloAvktTZOWIhmwQZmHTZmnIZUWIhRgcZOaYCmnFZRHIhmeMZOWj5RsYZU0Q5AwYZO3IhRnx5O0Q5JGyZmWxWAScZRyQWAMJZO0FWAMV5msICRWxZmnQ5RnYZR3QCRsB5U02WJMfCJyQ5mHTZO325AsYCJnQWRhJZOWQZmMfCR3Q5JhHuT3QhRHF5RWTCmnYCV5I5mcM5Ra2hmMfCRsQWJyBZO5YhRsT7O0QhRpM5UaYCJGJ5mHjWRyB5V0T5RSy7R5IWRGc5UWBhRnAZmajZUQxZOnjWJnICRwjZOyj5Ro6CRHT5UnQWRWAZmoqCAoJCV3Q5VQTCA325myBZR32CAwA5S285VrjZRWThJcyZRs2CAaQZRnYhJpcCV0xhOBBZRgXCAoyCOnxhVBIZRoXhRHACJHxWJCcZRyQhRwj5Oa2CVNJZUgqZRcJCO52hRoJ5OW2ZmwFZmy25RyI5mHB5JyFKRaxhUIfCTGqZRy27RWxWV2ACAH25RcM5V5xhJWQZUG4vktjZRGqWJHqKlpl3f2fK1eBH15DvkEHuTCy7fxAoOSBKUCM7OIxuVc8vkEN7UoHuT5qKVS2HTCM7fxYHF28CRMV5U325JMyCJyIhRcM5U3F5RsACA5ICRcJ5UWBCJsx5V0Q5AyxZmw25RMM5RwICJwxZO3jhRsYZUnI5AcJZmwQWRsYCR5QZmpJ5UnTZRsjZmyI5JcJZO5QCRnA5msQhJhMZmsYhJsj5m0QWRhJZmwTZmMfZm3QhR0IZO0TvktIZmaYhmn2CJ3IWRhy5R0xWRnx5O3Qhm02Zmsx5Js2CTnQhOIyZms2hJnQCT3jZmpM5VWx5A3QCVnIZUFc5R5jWAnYCmaIZRMV5JSq5JMy5V0ICJgV5VGNZRnA7RyYZmGcZmHTCRMc7VaYWAhfZOSqWJHF5msxWJyx5m52CmpHuAM45RyFCVWxWJyj5OaBZRccCTn2WJWIZRC65AyF7O5YhJaTZRc6hRyA7myY5mWIZUnA5myTZmy2WJpc5mo4WRyA7V5x5meM5mHY5AwQ5O52WRsx5m3xZRwjCO5xWTyFZRCXZmoV7VnxZOFVZRGXCRoM5O5x5m3QZU3T5JccKc285Rcc7JnYuVN2hj3X3VMBo1pAZc28vkEJhOQqHYeMoOcJWUIXi9xN7F28KUFlvkEJuVNMH1CJWUQqoYCHuAH2CRCy5V32CJMM5AWIZmCfCA52CAyB5TaIZR5TCJs2ZRaB5mHICAwA5V02ZRCc5AnjWJwICO02hRSV5TnIhmwT5mw2CRHY5myjZmCfZmH2Cmpc5TWICmwT5m0ACm3B5mnQWAwBZR02hmGJ5mwQ5JpcZm325Jpy5myYhRwFZS28hJpVZU3A5RMJ5AyQCRCV5UWACJGc5maYWRpJ5U02WAcM5OSXZmwQ5JWAhmyACm5jCAa25U3BCRMc5Ae6hRCJCm5AWJWF5An25Jax7JW25JaY5T3YCAeyKR5ACUBFZRwFCJpJZms2COQAZRyQZRpJ7Jsx5J3Y5U0I5RGJ5JHjvkEVKJHICAsF5UaIWJ3TCVnICJyQ5V3Q5AnY7RaIhO2xZRWTZmnx7RaICT2YZR5QhRnxCU0I5Rpf5JnjhRGcKVWIWUxy5RWT5m3FZRHjZR3Q5V32CA3B5U0jhJWB5RhNCRnB7max5UxJ5Re6CRSy7O5x5m3j5U0B5JnjZOaIZUNHuT0ICUxVZ9x4HOHfukGro1e2HANHuF28WTequlCBWveNWTSX71yqKUEHuTc6hF28Wjx4WvpAWTSquloAvkEy5mnA5RGc5OaQZR5F5U0ACJHj5mHxZRCf5RaAhmhc5m0BhJwj5R32hm3Y5m3ACm5x5Jw2hReJ5O5IZRCM5RH25Apc5mnYhJwj5JnACJsx5ms2CACV5RH2ZRwT5AyjZmwjZOn25mcV5mWjCmwjZO32CJMf5ma2hJpMCA02hmMHuAw2Zmy25A3Q5mCJZmwAWJnB5AHjCApMCAs2WRSy5OnxCmwI7mW25m3Q5A0xCJaj5JwBZRnBCm3QCmCf7UnAZmeM5TaT5J5I5O0BhUQT5O3BhRCVCAsBCTyT5TS65RyACJW2WRsY5mgqWJccZO32hVQjZRHjWASc5U0j5RGy5c28ZOrj5UaQ5RnI5U0jhmWx5R0xhR3TZm3ICTBx5UGXCmcyCV0ICUFV5RGX5JcyZmHI5J0B5Ry25mGc5V5jWTrx5USqCAnACV0j5RcV5J3j5R3T5myjZmnY5VWTCAnxKRWIZU2QZRGqWRn27UWICV2BZR3jWJnTCO3ICmMV5US4vktF5Ueq5myqKlpl3f2fK1eBH15DvkEHuTCy7fxAoOSBKUCM7OIxuVc8vkEN7UoHuT5qKVS2HTCM7fxYHF28hRyj5JwI5AyYCTWjCApy5VWTWRyBZO5jCJpc5J0FWAyT5TWxhRw2ZRwIWAcy5V0jhmSfZUaAhRyj5O5jhRpMZRHxCRcMCJ5xWRnQ5VaY5myxZRnjCRwxZUaxCRGJ5R3xCJH2ZRwQWRcM5R3xWJHAZRyYhRccZR3x5JWIZRsYvkt2ZRWQWR3ICA3jCRHj5Jy25A3B5R0x5RWjZR525RyBZm3x5T2xZR5I5RGyZO0AZRnF5Aw2WR5ACAnjWUBA5J0A5RGyCRnj5Ryx5Ahq5AyA5T0jhRWx5ASNhJ3j7R3QWJWIZRWQCmcJ7V0QWAofZUGqhJs25Un2WRwT5U0IWRSHuAh4WApcCA325mwA5RHFhJwYZOaIhJoM5me6WAwT7O0QCRgJ5mh6hJpc7mwQ5moJ5OaBhRpyZU3IhmSc5RG4WApc7Vn2CA0F5UnQ5ASM5RsIhmy25Ry25RnFCRy2hOyF5OCX5JsT7Jw25OFM5OSX5JMf5R52ZRCc5O0Y5JwjKc285RwQ7JwQuVN2hj3X3VMBo1pAZc28vkEJhOQqHYeMoOcJWUIXi9xN7F28KUFlvkEJuVNMH1CJWUQqoYCHuAsA5mWYCUnAZmSyCJHBZRhV5mHACR3ACJsBhRWjZU0ACmH25T0BCm5ACUnA5JWFCJyF5RCyCT5AhmaACJaBCACy5A0AhRpc5AaFWAWj5UaACm5BCJwBWA5x5AaThmgJ5AyIZR5I5JHACmgy5AwIZm5A5R5A5R5T5Aw2Zm5x5g285mCJ5J5T5mnTCV5ICJWjCmaT5R0B5A52CA5jCmwA5A3I5AhXCm5YCRwThJGyCT0FCJoyCm5YCRnxCJh6hRW2CAyTWJyxCJaA5Aof7V0AWJH2CV52hJHAKRWThOFf5JWIWR5F5JwAWUQx5V5IhJ5Q7J0jZmgMCmyBhRgVCR3FvktTKVaBZmn2CmHBWR0QZUaBCA3xCUWICAaF7R5BZOIc5J3xhRaB7RwBhU2B5J5IWRajZm0BZR5ICUaFWAgyKJ5BCOQYCOaxCRgc5V0FCmgJCR3Ahm0BCmaF5mcJCmcNCmey7O5jZUQxCmS6CAaB7Oaj5m0QCm5Y5AaI5RWB5ONHuAsBZUQ25fx4HOHfukGro1e2HANHuF28WTequlCBWveNWTSX71yqKUEHuTc6hF28Wjx4WvpAWTSquloAvktT5OaAZm325O5QCR5j5UaAWAHI5O0xCACf5U5AZmWI5msB5Rpc5RW2hJGM5O5AhJ5A5Jy2hRaY5OWIhR5Q5R32WAwA5m0YCAwT5V0ACmsQ5ma2ZRCJ5U525JwF5T3jhRwFZO02hJcJ5O0j5ApMZmn2WRMy5OW25ApfCTa2hmMHuA325JcJ5AyQCm5jZO3ACRSJ5T0jhJwACAw2CASJ5m5xZRpy7O525JGV5Awx5mef5JWBWAnjCm5QCJ5F7U0AZmaj5AyT5JCf5OaBWUQY5mWBZmCfCT3BCUcy5AG65AcyZU325my25mhqWAyTZO325UQYZU3jhJSy5U5jCA3T5c285TrA5RWQhmnQ5RwjWAWF5R3xhmGJZmnIhUBY5UGXWAyQCJnIZOBI5UgXCJcVZOnIWAgV5R52ZRGc5JHjhVNc5RMq5JSJCJ5jCRyY5JWjhR3B5msjCmnx5V5TWAnjKR0IWVIcZRgqhRSV7U5IhU2AZUnj5RSMCOaIhJsA5UG4vkEy5Ueq5JyqKlpl3f2fK1eBH15DvkEHuTCy7fxAoOSBKUCM7OIxuVc8vkEN7UoHuT5qKVS2HTCM7fxYHF285R5QCJHF5mCM5U3T5RaICV32hJ5T5VaThmeyCVaxCA5YCT5A5mey5AyFCR5FCV5TWJgy5A0YhR5jCO5ThRaI5AnA5A5F5O0A5RgJCVnIhJ5Q5TnT5Ra25AWACmWYCU3ACAnI5AWj5J5xCU5AhmSf5T3I5J5j5AwAhRw25A3IvktF5TnjhmWT5RHTCAnYCJ3B5JhMCR5A5mwQ5T0BWA5x5JHAWVIf5TnFWAWQ5VaY5J0TCAWB5RHQ5R5T5VBICV3Y5RhJZRnTWJ5FCTeqhJCMCTnTZmwACThN5AWB7UWjCA5x5AnjhJCy7VnjCAnF5ApqZR3ACU3BZRaICU3FhJgHuAc4WAaI5RsBCAa2CUnxWJaI5JnFWJSVCmh6hmey7OajhmyYCmo6WAa27OajZRnFCm0QWAef5TaFCRgcCRG4WAec7JnB5RyACR0jhR0ICU5FWRCVCRsBWR0IZRwBCVyBCmoXWJ3Y7JnBCVBjCmcXWAGyCU3BWJHjCm3I5mecKc285maj7J0juVN2hj3X3VMBo1pAZc28vkEJhOQqHYeMoOcJWUIXi9xN7F28KUFlvkEJuVNMH1CJWUQqoYCHuAHAWRWBCRWAWAnjCV3BZRWT5m0AWA3xCJ5B5AWYZRwA5mH25AWBZR5jCRWA5mWxCJsFWA5YCA3AhRajCJWB5R5Y5AsAhmwF5AHFhRhJ5UWAZRCcCJ3BCmCJ5T0TCA0I5AWIhm5Y5J0ACJ0T5AwIhRCM5R5ACmCM5A52CmCf5g28ZR5T5JyTCAnjCJWIWAW2CmnTZmgV5As2WR5ACOaAhJ3I5AeXCA5ACRnTWRGcCT5FhRocCmaYCJnYCJM65RhVCAaTZmyICJsA5JHQ7JyAWRHYCVW2ZRHYKR3T5UFc5JaBCJCV5VWA5Vxf5J0IhJ527JWjWJ0YCmyBWJgcCR5FvkEJKJsBCASMCmaBCJgJZR3BWJ3xCRWICReJ7RyB5T2A5JHx5ma27RwBCV2x5J3ICJa2ZmyBZmCJCRHF5JgcKJ3BCVQICmHxZmgy5J0FCAgcCU5AhJ0xCO0F5JyBCOCNhRaY7mnjCVxyCmo6CRaT7mWj5mgcCOnY5AaB5UnB5TNHuAaBWVQx5fx4HOHfukGro1e2HANHuF28WTequlCBWveNWTSX71yqKUEHuTc6hF28Wjx4WvpAWTSquloAvkEM5AHTCmgV5A0IWRhfCOnT5mpJ5T3jZmWACOaTWRyY5TWYZRCfCOnACR0I5T0TCRhyCUnAZmH25A3BWJWFCmsACJ5A5Tn25J5ICRaT5RSy5AnAZmWICO3A5RCyCJyFCm5Y5RaACRGJ5T3FWJCc5UaAZRnA5TnACmCV5mnAWRSHuTWAhRGMCJHIZmWI5R5T5mefCJWFWA5B5maA5mef5TnjhR5x7mwA5R0xCJajhJHFCRsYZReMCT3IWAWj7UWThJHjCV0xCRhf5AWYWVxc5AwYWAhJ5mnYWUcMCVS6CJ3FCU3A5mGy5TGqCJ3A5R0AWTQQ5JnFCJaACmsFWA0TCg28hUrxCOnICAaBCOWFCRcMCO3jWR025U5BWVBQCmCXhmGMZRHBhVBxCOGXWR3F5RnBhRsFCOaAWR0QCRwF5VNfCmgqZmaTZR5FWR3jCRnFCm0I5TaF5RaQCRnxWJaIKUaBZU2B5JoqCmaF7U5BWVIf5V0FhmaxCA5BWRnBCOh4vktICOSqhJ3qKlpl3f2fK1eBH15DvkEHuTCy7fxAoOSBKUCM7OIxuVc8vkEN7UoHuT5qKVS2HTCM7fxYHF28WR52CVWFCR5x5UnTWAa2CJw2CJ5Q5JHTCAeVCJsxZR5YCTaACAaj5TaF5J5BCVnT5Jgy5AHYhm5TCmyTWAeM5A5AWJ5Y5O3Ahm0FCVaIhJ5x5AWTWRey5AnACJW2CU3ACRnT5AajCACfCR5AWAnI5A3IWACM5AnAWApy5AnIvkEy5Asj5mWT5R5T5AnACVWB5mhVCRnA5RpM5TnBWJ5x5VaACO2B5AnFCJhc5JnYCA0QCTaBWJH25U5TWVBxCJWYWRWAZRnTCACVCTSq5A52CTaT5mpcCAMNhRWA7RajZRWF5AajhmCy7VnjZRSc5AhqhR3jCR0BhmaQCRaFZRgHuTG4hReM5RwBhJaFCRyxWReJ5JwF5mnTCmM6CAaY7OWjZmyxCmo6hmaY7m3jhmnjCmaQWRaj5TaFCAgyCRh4ZReJ7VnB5AcfCRHjWJgyCR0FhmCVCRyBCJgMZUaB5UyTCmpXWJ3F7JWBCOBYCOGX5AGJCRnB5JoJCOnI5ReJKc28hmaI7VajuVN2hj3X3VMBo1pAZc28vkEJhOQqHYeMoOcJWUIXi9xN7F28KUFlvkEJuVNMH1CJWUQqoYCHuTnA5RhMCR5ACJSMCJsB5Rhc5OaA5mGJCJnBZmhfZRnAWJHA5A0B5mCMCRWAhmhcCV5F5m5ICTWAWJaTCVWBCA5F5T0A5Rwj5A3FCmhy5RwACJCMCJ3BhJ5F5AyThJ0Y5AsI5JCM5V5AZmgy5A5I5A525UaAWJ525T325R5B5g285RCV5VnT5JnACJHI5mWxCmHTCJgV5T025JCyCOaA5JGf5AeXCJCcCRsT5R3ICAnF5RH2Cm5YhRnACVh6hRhfCA5TZmyxCJ3A5RHI7VaA5JHYCVW25AHBKRnTCTFf5JyYWR525J5ACTQT5J0IZRCV7V0jZR02CmaBZR0FCU5FvktTKJwBWRnICOnB5A0TZUnBZm3ICU0Ihma27U5B5U2Q5V3x5Ref7R5BCU225VnICJeyZO0BWA52CRnFhJgfKJWBhVQYCm0xhR0I5J3F5J0BCUaA5m0TCOnF5JcVCmgNZReV7mHjZOQjCOS6WAaQ7Oaj5A0ACmwYhmef5RwBWVNHuAnBCOQI5fx4HOHfukGro1e2HANHuF28WTequlCBWveNWTSX71yqKUEHuTc6hF28Wjx4WvpAWTSquloAvkEy5mWAZRGJ5m5QZRCV5RWA5AH25O3xCmCc5U3AhJWF5maB5JwB5Ua25JGy5mWAZm5Q5Vn2WJaY5m5ICR5T5U02ZmpM5mWYZRpc5JWAZRMc5OW2ZR5x5Rs2WJwF5AwjCRwjZm32ZRyx5myjhmpfZO325AsF5mW2hmwxCAy2hmMHuAy25RyI5TWQCJ52ZOnA5AnI5AWjhJwFCA52CJSf5mWxhJwT7O325R3F5Tax5JeM5JyBZmnACOnQhJ5T7R0AhReV5AaTWA5B5OaBZOxy5mwBWJ5ICAsB5OcM5Ah6hmyxCU525mcf5mgqhJccZmy2WTQFZRWjZRnx5U3j5JGf5c285TrF5UaQCJnx5U5j5AWx5UWxCJ3AZmwIWVBB5UgXWJcVCJ0ICOBj5ReXhmyjZOaICJ0Q5Rs2WRGf5JHj5TrF5RhqhRnQCVnjCJyx5V0jCJ3x5O0jhJnx5JyTZmSfKR0I5O2FZRGq5Jnx7RyIZUIyZU3jWJnACm0ICJMy5Rc4vktx5UeqZRyqKlpl3f2fK1eBH15DvkEHuTCy7fxAoOSBKUCM7OIxuVc8vkEN7UoHuT5qKVS2HTCM7fxYHF28ZmyT5J5IhRcfCAWjWAwx5VWThmcJZmHjZmwF5V3F5Jcf5AHxWAw2ZUnIWAcy5V5j5RSfZRWAWRcf5m3jCmwIZRnxCmy2CJaxWAnx5JnYhmyIZR5jZmw2ZU5xZm3I5U5x5RHTZU3QWJyY5U0xCmHFZR0Y5AyIZU5xWAhVZRHYvktxZRwQCR3FCTnjWRHx5J32WJGy5RwxhRhJZUW2WRcJZm3xWV22ZU0ICR3AZmWA5JnF5Aw2hR5YCA0jZUBB5VnA5mGcCUnjCRyQ5TeqCRyQ5AwjWRhM5ACN5JGf7RwQ5J0BZRyQWRcJ7JyQCAoJZRhqZRsB5U52CJwQ5RWIZmSHuAc4CAwQCAW25Apc5U3FCJpJZmyIhJoy5OC6CAwQ7m0QWJ0j5OG6CApJ7mwQWRoJ5OaBWAwIZU3ICJSM5UG45JwB7Jn25R0B5RwQCmSJ5RyI5Rcy5Rn25AnQCRw2WTyQ5mSXWAsI7JW2WVFf5moXhRsB5R02CR5A5mnYWApJKc28hJwY7JyQuVN2hj3X3VMBo1pAZc28vkEJhOQqHYeMoOcJWUIXi9xN7F28KUFlvkEJuVNMH1CJWUQqoYCHuT0x5R3A5RHx5mHQ5V32WJ3YCVnxWRsQ5J32CA3QCRHxZR5BZRy25Jyj5U3xWJ3Y5JWIZmcM5AHxZmpy5Js25Ay2ZRWxhmWjZRsIWA3TCAHx5Acf5Jn2hJyBZUWjCJn2ZUaYhJyxZmWxCRnQZRwYZRcMCAwxWRyTZRaTCJyBCF285AcJZmajZRoJ5JHY5JGJ5m3jWASMZRWTCAyB5mwxZRsIZUGXCmcf5U3jWRMV5AyI5A5x5OWA5RoM5Je65A3B5A0jZR0j5V3xCm5Q7V5xhR5Y5V0TCRCVKRwjCTBjZOnT5JyIZRaxWTxJZmaYCAcc7V0QhJSM5Oa25Rnx5R3IvktBKJy2hRHI5m52CAn2CRs2WJMf5RHYWRpM7Rs25V2YZmsFCAwx7Ra2ZU2jZm3YWJpfCmH2CJyQ5UnICAnQKVa2ZOQA5mwFCJSyZm3IZmSJ5RWxCAnA5O3I5RgV5meNhJwT7mwQhOxV5mc6CJwF7msQ5mnx5OnA5mpMCAW2CONHuAW2COQAZkx4HOHfukGro1e2HANHuF28WTequlCBWveNWTSX71yqKUEHuTc6hF28Wjx4WvpAWTSquloAvkEf5JWFCJaY5V325J0Y5T5F5JyA5VaI5m025AHFWRMc5V0T5AGc5A3jhJaT5J0FZR0jCmsjhRWF5JWAhJgc5T3jCAGV5VnxWJGyCm0FCRpM5V5jhJgM5TnjZR3ACU3B5m3I5OWjCmSy5VaBCRGf5O5jhmpJ5V3jhmGVZUnjZmpHuAnjWRSMCRa2hJgy5m3FCR5BCR3BhRGVZRnjCR5j5JWIhR3I7mnj5JeyCRaIWAhJCO3Thm5xCV02WJ0Y7U3FhmWFCRsQCJ0Y5JsTCOQF5JwTCJgyZR5TZOyYCUh6CJSM5mwjZRGf5JgqCASM5m0j5OxM5RWB5mCM5T3BhJaFCS28CVrI5T32CJ5T5AHBWJsj5AHIWAaj5m5AhOB25ApXCASMZO0AhUBA5TgX5JSV5mWACRHx5A3jZmecCm0BCVNV5TgqWJ5YZOWBWRSfCOaBCRaF5VnBWACVCOnQWR5xKR5AWV225RSqhJ5B7UaAhO2F5U3BWACyCV5AWJpy5Th4vkEM5AMq5RnqKlpl3f2fK1eBH15DvkEHuTCy7fxAoOSBKUCM7OIxuVc8vkEN7UoHuT5qKVS2HTCM7fxYHF285Rcc5JaICJyICAWjCRwj5JyTWAyTZOajZmwI5J3FCJyT5T3x5Aw2ZRHICmcc5V0jhRnBZRnA5JyQ5majhJpMZRnxWRyjCJnx5AnI5V3YZmyQZRsjWJpyZU0x5R3x5Rsx5mofZRyQCAyx5UWxWRHxZR5YWRcMZRyxWJW2ZRnYvktYZUWQWAGfCA0jWAHA5Jn25A3Q5UnxCAWFZU02ZmyTZmHxWV2TZU5ICJ3jZO3A5RnA5Ay2hR5BCAWjCUFV5V5Ahm3ICRwjZmyY5TSqCmcc5T5jWAWI5AoN5AGy7RsQ5JsBZR5xZmyj7VnQWAocZUGqCAMc5Ra2WRpy5RwICRSHuTe4hJwICT02CmwT5RnFCJwFZm3IZRoJ5mh6hmwQ7mnQ5J0T5mh6WJwA7mwQCAHB5O0BhRpcZRaI5Rnj5RM4CmpV7Vn25RgM5R3Q5RnA5RnIWJyQ5Rs2ZmSfCUa2hUyI5mcXZms27V32ZUBY5mcXhJMM5Ry25ACM5OnYZRpfKc285mwI7JHQuVN2hj3X3VMBo1pAZc28vkEJhOQqHYeMoOcJWUIXi9xN7F28KUFlvkEJuVNMH1CJWUQqoYCHuA3A5JWxCRwA5JSMCVnB5Jhf5m5AZR3BCJHB5RWYZR0AWJH25AsBCmCcCU0AZRWBCJsFhmCfCT5AhJecCJnBhR5B5AWAWJwQ5AaFhJWY5RwAhJ52CV3B5m5A5AyT5Agc5AaIWJCc5V3ACJ0I5AsI5J5A5R5ACm5x5An2hJ5Y5g28CACf5JHTWRScCJsICmWYCm3T5A0A5As2WA5BCmwAWJGJ5TCX5A5TCRyTWJ3FCAnFhmocCmaYZmSVCVC65mWxCTnThRcyCJwAhmHx7JyA5JHYCJH2WJHAKUaTCVBY5V5ACm5Y5AsA5TxV5JHIhR5Y7VWjhm0xCmWB5JgcCRsFvktBKJ0B5JnjCmyB5m0jZUnBWR3jCU3I5Aax7RaBCT2I5JHxCRef7RwBZO2B5V0IhRaxZmaBZmCyCU3FWAgVKV0B5UxcCmaxCJgf5JsFCmgVCRHA5mgVCmwFWJy2CmCNZRaA7O0jCOxfCOg6WJaT7mnjZm0jCm5Y5AaB5RyB5UNHuTWBCTQY5fx4HOHfukGro1e2HANHuF28WTequlCBWveNWTSX71yqKUEHuTc6hF28Wjx4WvpAWTSquloAvkEM5TnT5mgy5TnIhRWjCmaTCRpM5AHjCAhJCO3TWAcc5T0YCR52CO5A5RgM5AWTCRWjCRsACRoc5T5B5RhVCO3A5ACV5Ay2WACfCR3T5RnT5A5AhmWFCOWAWR5jCJHFhmCy5RWAZmGM5AWFCm5Q5R5A5ASJ5AwACJCf5O0ACASHuAnAWRGMCVnIZmWQ5R5TWReVCVWF5A5Y5m5AWAeJ5AsjCRCM7mnA5AgMCV5jWJoVCUWYWRaACA0ICmWT7UWThRHFCJnxCmWI5A5YZOQA5TnYhRWA5O5YCUyACVC6WJ3xCmyAWRCc5AcqhR3B5R0AWUQA5JaFWRaFCOaFZR0jCg28WTNyCOnIWRa2COaFWRccCm3jCm0F5UnBCTBYCOCXWJGJZU0BhUBFCmpXWA3I5UnBCJsFCm3AZm0FCU0FWTrICOhqWJaAZUWFZRGJCU3F5J0I5AWF5AeyCR0xhJeMKR3BCTIy5VeqCJaQ7UnBCV2B5JsF5AaACT5B5mnTCOG4vkEcCmoq5R3qKlpl3f2fK1eBH15DvkEHuTCy7fxAoOSBKUCM7OIxuVc8vkEN7UoHuT5qKVS2HTCM7fxYHF285RyB5V0ICJcfCTWjZmpV5JaThRyFZmWjWApJ5JsFWAyx5AwxhJw2ZR5IZRcy5VWjhmnIZRyA5mcc5myj5AwFZRaxCmcMCJ5xCRSy5V0YhmyYZUnj5RpVZRWx5R3x5R5x5moyZU3QWJcV5Rax5JH2ZRaY5RcMZRHxCAhyZR3YvktYZR0QCmGfCT3jhmHQ5Jn2WJ3T5R0xCmWIZU525JyBZO5xZU2IZRaICR3AZmaAZmSV5Aw2hJ5xCAsjCUBB5V3AWR3BCRWjWAyQ5Aoq5myj5T3jhJhf5AGNhm3B7RwQCmnQZRyx5AcV7JHQCmHTZUCqhJsB5Ua2hmpf5RyI5JSHuTG4hRwICAa2hRwT5U3FhJwQZOWICmoc5mg6CJpJ7O5QZmgJ5mc65mpV7mwQ5AHx5OaBhJpyZRnIZRn25Ug4ZRwA7J02Cm0B5UnQ5AnF5UaIhRcy5RH2WRnICUW2CVyI5mcX5As27Va2WVFf5OhXCmsj5Rw2Cm5I5OnYCAwYKc28hmwj7JHQuVN2hj3X3VMBo1pAZc28vkEJhOQqHYeMoOcJWUIXi9xN7F28KUFlvkEJuVNMH1CJWUQqoYCHuAsACmWxCUnAZRSVCVaBCRhf5O5AhmGcCV5BhmW2ZRnACAH25A0BhRCcCU3A5JWFCVWFCR5FCAwAWRaFCJ5BCm5B5T3AWApM5T3FhJWT5R5AhJ5FCJyBWRCy5TaThR025AWI5m5I5VnACR0j5AWICm5B5UaAhJ5I5TW2Zm5B5g28hR5I5J5TWJnQCJWIhRhfCmWTWA0Y5T32hJ52CmyA5m325ASXWR5BCUnTWR3xCAHFWJH2CmWY5mn2CJp6ZRhMCTaTWJcVCJaAZRHB7JaAhmoVCVn2WJHFKU0TCTFM5JwT5J5j5AnAhVQT5V5IZRCM7V5jhJ0xCmnBWA0QCRWFvktjKJ0BZmnBCmyBWR0xZUWBhm3FCU3IWAey7U0BWTIy5Jwx5RaY7RwBCUIc5V0IWJajZm0BhJ5FCR0F5mgMKJ0B5TQBCmaxWJgJ5VnFWJgVCRWA5AgVCm0FZRcMCOeNhmec7mwjCVQ2CmS6WRaF7m3jCJ0BCmaYhmeV5RnBhVNHuAsBCOxc5fx4HOHfv92fWUIX9UxP7V0fZVhM71Ccza==";
        // /[A-Z0-9]/gi

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                String magicString1 = "AzFpUnL5uICH63DtSVmv1x4P7b9Z8y0idcrGhQJNgTelK2BMEWRfYjowOakqXs";
                String magicString2 = "wASN0luxq3k1DYm69gV8FIQPb7EhtjBfoHLOMaGCsRicdTp5nUezWJr2ZKy4vX";

                sb.append(magicString1.charAt(magicString2.indexOf(c)));
            } else {
                sb.append(c);
            }
        }

        System.out.println(sb);
    }
}
