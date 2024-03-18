package ratel.crack.com.taobao.etao;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.api.RatelConfig;
import com.virjar.ratel.api.RatelEngine;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.extension.JustTrustMe;
import com.virjar.ratel.api.rposed.IRposedHookLoadPackage;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.api.rposed.callbacks.RC_LoadPackage;
import com.virjar.ratel.api.xposed.IRXposedHookLoadPackage;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import cn.iinti.sekiro3.business.api.SekiroClient;
import cn.iinti.sekiro3.business.api.fastjson.JSON;
import cn.iinti.sekiro3.business.api.fastjson.JSONObject;
import cn.iinti.sekiro3.business.api.interfaze.ActionHandler;
import cn.iinti.sekiro3.business.api.interfaze.HandlerRegistry;
import cn.iinti.sekiro3.business.api.interfaze.SekiroRequest;
import cn.iinti.sekiro3.business.api.interfaze.SekiroRequestInitializer;
import cn.iinti.sekiro3.business.api.interfaze.SekiroResponse;
import cn.iinti.sekiro3.business.api.log.SekiroGlobalLogger;
import cn.iinti.sekiro3.business.api.log.SekiroLogger;
import cn.iinti.sekiro3.business.netty.util.concurrent.FastThreadLocalThread;

public class HookEntry implements IRposedHookLoadPackage {
//        private String CLIENT_ID = Build.MODEL + "_" + Build.VERSION.RELEASE + "_89AX07FHJ";
//    private String CLIENT_ID = Build.MODEL + "_" + Build.VERSION.RELEASE + "_89CX08D9U";
//    private String CLIENT_ID = Build.MODEL + "_" + Build.VERSION.RELEASE + "_89NX0CMV8";
//    private String CLIENT_ID = Build.MODEL + "_" + Build.VERSION.RELEASE + "_8CCX1MCNL";
//    private String CLIENT_ID = Build.MODEL + "_" + Build.VERSION.RELEASE + "_92DEC0A291";
//        private String CLIENT_ID = Build.MODEL + "_" + Build.VERSION.RELEASE + "_92BBF2E2BB";
    private String CLIENT_ID = Build.MODEL + "_" + Build.VERSION.RELEASE + "_92B6728EBA";
//    private String CLIENT_ID = Build.MODEL + "_" + Build.VERSION.RELEASE + "_92MAX01J61";
//    private String CLIENT_ID = Build.MODEL + "_" + Build.VERSION.RELEASE + "_92DE89CEE9";
    private static final String TAG = "LIVE_HOOK";
    private static boolean serverOpen = false;
    private static boolean hookSpdy = true;

    public enum UnitStrategy {
        UNIT_TRADE,
        UNIT_GUIDE,
        UNIT_NULL;
    }

    @Override
    public void handleLoadPackage (final RC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.contains("com.taobao")) {
            JustTrustMe.trustAllCertificate();
//        if (lpparam.packageName.equals("rca.rc.tvtaobao")) {
            if (lpparam.packageName.equals(lpparam.processName)) {
                if (!serverOpen) {
                    serverOpen = true;
                    CLIENT_ID += "_" + lpparam.packageName;
                    new SekiroClient("mtoprpc", CLIENT_ID, "120.27.226.218", 5612).setupSekiroRequestInitializer(new SekiroRequestInitializer() {
                        @Override
                        public void onSekiroRequest(SekiroRequest sekiroRequest, HandlerRegistry handlerRegistry) {
                            handlerRegistry.registerSekiroHandler(new ActionHandler() {
                                @Override
                                public String action() {
                                    return "send_mtop_request";
                                }

                                @Override
                                public void handleRequest(SekiroRequest sekiroRequest, final SekiroResponse sekiroResponse) {
                                    JSONObject requestJsonModel = sekiroRequest.getJsonModel();
                                /*
                                    MtopRequest mtopRequest2 = new MtopRequest();
                                    mtopRequest2.setApiName("mtop.taobao.wireless.shop.fetch");
                                    mtopRequest2.setVersion("2.0");
                                    mtopRequest2.setNeedEcode(false);
                                 */
                                    Class<?> MtopRequestClass = RposedHelpers.findClass("mtopsdk.mtop.domain.MtopRequest", lpparam.classLoader);
                                    Object mtopRequest = RposedHelpers.newInstance(MtopRequestClass);
                                    RposedHelpers.callMethod(mtopRequest, "setApiName", requestJsonModel.getString("api"));
                                    RposedHelpers.callMethod(mtopRequest, "setVersion", requestJsonModel.getString("version"));
                                    RposedHelpers.callMethod(mtopRequest, "setNeedEcode", requestJsonModel.getBooleanValue("NeedEcode"));
                                    RposedHelpers.callMethod(mtopRequest, "setNeedSession", requestJsonModel.getBooleanValue("NeedSession"));
                                    //mtopRequest.setData(ReflectUtil.converMapToDataStr(mtopRequest2.dataParams))

                                    RposedHelpers.callMethod(mtopRequest, "setData", requestJsonModel.getString("data"));
                                    //RemoteBusiness build = RemoteBusiness.build(mtopRequest, str);
                                    Class<?> remoteBusinessClass = RposedHelpers.findClass("com.taobao.tao.remotebusiness.RemoteBusiness", lpparam.classLoader);
                                    Object build = RposedHelpers.callStaticMethod(remoteBusinessClass, "build", mtopRequest);


                                    // build.protocol(ProtocolEnum.HTTPSECURE).useCache().reqMethod(MethodEnum.GET).registerListener
                                    Class<?> ProtocolEnumClass = RposedHelpers.findClass("mtopsdk.mtop.domain.ProtocolEnum", lpparam.classLoader);
                                    Object[] enumConstants = ProtocolEnumClass.getEnumConstants();
                                    Class<?> MethodEnumClass = RposedHelpers.findClass("mtopsdk.mtop.domain.MethodEnum", lpparam.classLoader);
                                    Object[] enumConstants1 = MethodEnumClass.getEnumConstants();
                                    build = RposedHelpers.callMethod(build, "protocol", enumConstants[1]);
                                    build = RposedHelpers.callMethod(build, "useCache");
                                    int reqMethod = requestJsonModel.getIntValue("reqMethod");
                                    if (reqMethod == 1) {// post
                                        build = RposedHelpers.callMethod(build, "reqMethod", enumConstants1[1]);
                                    } else {//get
                                        build = RposedHelpers.callMethod(build, "reqMethod", enumConstants1[0]);
                                    }
                                    Class<?> IRemoteBaseListenerClass = RposedHelpers.findClass("com.taobao.tao.remotebusiness.IRemoteBaseListener", lpparam.classLoader);
                                    Object IRemoteListener = Proxy.newProxyInstance(IRemoteBaseListenerClass.getClassLoader(), new Class[]{IRemoteBaseListenerClass}, new InvocationHandler() {
                                        @Override
                                        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                                            if (method.getName().equals("onSystemError")) {
                                                String string = JSON.toJSONString(objects[1]);
                                                for (int i = 0; i < string.length(); i += 4000) {
                                                    int end = Math.min(i + 4000, string.length());
                                                    String part = string.substring(i, end);
                                                    Log.i(TAG, "invoke " + (i / 4000 + 1) + ": " + part);
                                                }
                                                sekiroResponse.success(string);
                                                return null;
                                            }
                                            if (method.getName().equals("onSuccess")) {
                                                byte[] bytes = (byte[]) RposedHelpers.callMethod(objects[1], "getBytedata");
                                                JSONObject jsonObject = JSONObject.parseObject(new String(bytes));
                                                sekiroResponse.success(jsonObject);
                                                return null;
                                            }
                                            return null;
                                        }
                                    });
                                    RposedHelpers.callMethod(build, "registerListener", IRemoteListener);
                                    String setUnitStrategy = requestJsonModel.getString("setUnitStrategy");
                                    if (setUnitStrategy == null) {
                                        setUnitStrategy = "UNIT_TRADE";
                                    }
                                    RposedHelpers.callMethod(build, "setUnitStrategy", UnitStrategy.valueOf(setUnitStrategy).toString());
                                    RposedHelpers.callMethod(build, "setBizId", 9998);
                                    RposedHelpers.callMethod(build, "startRequest");
                                }
                            })
                                    .registerSekiroHandler(new ActionHandler() {
                                        @Override
                                        public String action() {
                                            return "get_item_detail";
                                        }

                                        @Override
                                        public void handleRequest(SekiroRequest sekiroRequest, final SekiroResponse sekiroResponse) {
                                            String itemId = sekiroRequest.getString("itemId");
                                            Class<?> aClass = RposedHelpers.findClass("com.ali.user.mobile.app.dataprovider.DataProviderFactory", lpparam.classLoader);
                                            Object getDataProvider = RposedHelpers.callStaticMethod(aClass, "getDataProvider");
                                            String ttid = (String) RposedHelpers.callMethod(getDataProvider, "getTTID");
                                            String utdid = (String) RposedHelpers.callMethod(getDataProvider, "getUtdid");
                                            Class<?> MtopRequestClass = RposedHelpers.findClass("mtopsdk.mtop.domain.MtopRequest", lpparam.classLoader);
                                            Object mtopRequest = RposedHelpers.newInstance(MtopRequestClass);
                                            RposedHelpers.callMethod(mtopRequest, "setApiName", "mtop.taobao.detail.getdetail");
                                            RposedHelpers.callMethod(mtopRequest, "setVersion", "6.0");
                                            String data = "{\"detail_v\":\"3.3.2\",\"exParams\":\"{\\\"appReqFrom\\\":\\\"detail\\\",\\\"clientCachedTemplateKeys\\\":\\\"[{\\\\\\\"id\\\\\\\":\\\\\\\"1538383035450\\\\\\\",\\\\\\\"version\\\\\\\":\\\\\\\"138\\\\\\\"}]\\\",\\\"container_type\\\":\\\"xdetail\\\",\\\"countryCode\\\":\\\"CN\\\",\\\"cpuCore\\\":\\\"8\\\",\\\"cpuMaxHz\\\":\\\"1766400\\\",\\\"deviceLevel\\\":\\\"medium\\\",\\\"dinamic_v3\\\":\\\"true\\\",\\\"finalUltron\\\":\\\"true\\\",\\\"id\\\":\\\"670188381491\\\",\\\"industryMainPicDegrade\\\":\\\"false\\\",\\\"industrySupportItemThrough\\\":\\\"true\\\",\\\"item_id\\\":\\\"670188381491\\\",\\\"lastItemId\\\":\\\"\\\",\\\"latitude\\\":\\\"0\\\",\\\"liveAutoPlay\\\":\\\"false\\\",\\\"longitude\\\":\\\"0\\\",\\\"openFrom\\\":\\\"dtaodetail\\\",\\\"osVersion\\\":\\\"28\\\",\\\"phoneType\\\":\\\"Pixel 3\\\",\\\"screenHeight\\\":\\\"2028\\\",\\\"screenWidth\\\":\\\"1080\\\",\\\"soVersion\\\":\\\"2.0\\\",\\\"spm\\\":\\\"a2131v.24504623.feed\\\",\\\"spm-cnt\\\":\\\"a2141.7631564\\\",\\\"supportIndustryMainPic\\\":\\\"true\\\",\\\"supportV7\\\":\\\"true\\\",\\\"tl_allow_escape_flag\\\":\\\"false\\\",\\\"ultron2\\\":\\\"true\\\",\\\"utdid\\\":\\\"ZYOfkVSgYwMDAEIqD/TJo8Dq\\\",\\\"xxc\\\":\\\"diantao\\\"}\",\"itemNumId\":\"670188381491\"}";
                                            JSONObject parse = JSON.parseObject(data);
                                            JSONObject exParams = JSON.parseObject(parse.getString("exParams"));
                                            exParams.put("id", itemId);
                                            String cpuMaxHz = "1766400";
                                            String osVersion = "28";
                                            if (Build.MODEL.equals("Pixel 3a XL")){
                                                cpuMaxHz = "1708800";
                                            }
                                            exParams.put("cpuMaxHz", cpuMaxHz);
                                            exParams.put("osVersion", osVersion);
                                            exParams.put("phoneType", Build.MODEL);
                                            exParams.put("item_id", itemId);
                                            exParams.put("utdid", utdid);
                                            parse.put("exParams", JSON.toJSONString(exParams));
                                            parse.put("itemNumId", itemId);
                                            RposedHelpers.callMethod(mtopRequest, "setData", parse.toJSONString());
                                            Class<?> remoteBusinessClass = RposedHelpers.findClass("com.taobao.tao.remotebusiness.RemoteBusiness", lpparam.classLoader);
                                            Object build = RposedHelpers.callStaticMethod(remoteBusinessClass, "build", mtopRequest, ttid);
                                            Class<?> IRemoteBaseListenerClass = RposedHelpers.findClass("com.taobao.tao.remotebusiness.IRemoteBaseListener", lpparam.classLoader);
                                            Object IRemoteListener = Proxy.newProxyInstance(IRemoteBaseListenerClass.getClassLoader(), new Class[]{IRemoteBaseListenerClass}, new InvocationHandler() {
                                                @Override
                                                public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                                                    if (method.getName().equals("onSystemError") || method.getName().equals("onError")) {
                                                        String string = JSON.toJSONString(objects[1]);
                                                        sekiroResponse.success(string);
                                                        return null;
                                                    }
                                                    if (method.getName().equals("onSuccess")) {
                                                        byte[] bytes = (byte[]) RposedHelpers.callMethod(objects[1], "getBytedata");
                                                        JSONObject jsonObject = JSONObject.parseObject(new String(bytes));
                                                        sekiroResponse.success(jsonObject);
                                                        return null;
                                                    }
                                                    return null;
                                                }
                                            });
                                            RposedHelpers.callMethod(build, "registerListener", IRemoteListener);
                                            RposedHelpers.callMethod(build, "setUnitStrategy", UnitStrategy.UNIT_TRADE.toString());
                                            RposedHelpers.callMethod(build, "setBizId", 9998);
                                            RposedHelpers.callMethod(build, "startRequest");
                                        }
                                    })
                                    .registerSekiroHandler(new ActionHandler() {
                                        @Override
                                        public String action() {
                                            return "get_h5_item_detail";
                                        }

                                        @Override
                                        public void handleRequest(SekiroRequest sekiroRequest, final SekiroResponse sekiroResponse) {
                                            String itemId = sekiroRequest.getString("itemId");
                                            String ttid = "201200@taobao_h5_10.2.10";
                                            Class<?> MtopRequestClass = RposedHelpers.findClass("mtopsdk.mtop.domain.MtopRequest", lpparam.classLoader);
                                            Object mtopRequest = RposedHelpers.newInstance(MtopRequestClass);
                                            RposedHelpers.callMethod(mtopRequest, "setApiName", "mtop.taobao.detail.data.get");
                                            RposedHelpers.callMethod(mtopRequest, "setVersion", "1.0");
                                            String data = "{\"exParams\":\"{\\\"countryCode\\\":\\\"GLOBAL\\\",\\\"channel\\\":\\\"oversea_seo\\\",\\\"ultron2\\\":\\\"true\\\",\\\"_ultron2_\\\":\\\"true\\\",\\\"pageCode\\\":\\\"miniAppDetail\\\",\\\"_from_\\\":\\\"miniapp\\\",\\\"openFrom\\\":\\\"pagedetail\\\",\\\"pageSource\\\":\\\"1\\\",\\\"supportV7\\\":\\\"true\\\"}\",\"detail_v\":\"3.5.0\",\"channel\":\"oversea_seo\",\"id\":\"734887423407\"}";
                                            JSONObject parse = (JSONObject) JSONObject.parse(data);
                                            parse.put("id",itemId);
                                            RposedHelpers.callMethod(mtopRequest, "setData", parse.toJSONString());
                                            Class<?> remoteBusinessClass = RposedHelpers.findClass("com.taobao.tao.remotebusiness.RemoteBusiness", lpparam.classLoader);
                                            Object build = RposedHelpers.callStaticMethod(remoteBusinessClass, "build", mtopRequest, ttid);
                                            String ua  = getUa(lpparam.classLoader);
                                            HashMap<String,String> hashMap = new HashMap<String,String>();
                                            hashMap.put("x-ua", ua);
//                                            build.headers((Map<String, String>) hashMap);
                                            RposedHelpers.callMethod(build,"headers",hashMap);
                                            Class<?> IRemoteBaseListenerClass = RposedHelpers.findClass("com.taobao.tao.remotebusiness.IRemoteBaseListener", lpparam.classLoader);
                                            Object IRemoteListener = Proxy.newProxyInstance(IRemoteBaseListenerClass.getClassLoader(), new Class[]{IRemoteBaseListenerClass}, new InvocationHandler() {
                                                @Override
                                                public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                                                    if (method.getName().equals("onSystemError") || method.getName().equals("onError")) {
                                                        String string = JSON.toJSONString(objects[1]);
                                                        sekiroResponse.success(string);
                                                        return null;
                                                    }
                                                    if (method.getName().equals("onSuccess")) {
                                                        byte[] bytes = (byte[]) RposedHelpers.callMethod(objects[1], "getBytedata");
                                                        JSONObject jsonObject = JSONObject.parseObject(new String(bytes));
                                                        sekiroResponse.success(jsonObject);
                                                        return null;
                                                    }
                                                    return null;
                                                }
                                            });
                                            RposedHelpers.callMethod(build, "registerListener", IRemoteListener);
                                            RposedHelpers.callMethod(build, "setUnitStrategy", UnitStrategy.UNIT_GUIDE.toString());
                                            RposedHelpers.callMethod(build, "startRequest");
                                        }
                                    })
                                    .registerSekiroHandler(new ActionHandler() {
                                        @Override
                                        public String action() {
                                            return "batch_get_item_detail";
                                        }

                                        @Override
                                        public void handleRequest(SekiroRequest sekiroRequest, final SekiroResponse sekiroResponse) {
                                            String itemNumIds = sekiroRequest.getString("itemNumIds");
                                            Class<?> aClass = RposedHelpers.findClass("com.ali.user.mobile.app.dataprovider.DataProviderFactory", lpparam.classLoader);
                                            Object getDataProvider = RposedHelpers.callStaticMethod(aClass, "getDataProvider");
                                            String ttid = (String) RposedHelpers.callMethod(getDataProvider, "getTTID");
                                            Class<?> MtopRequestClass = RposedHelpers.findClass("mtopsdk.mtop.domain.MtopRequest", lpparam.classLoader);
                                            Object mtopRequest = RposedHelpers.newInstance(MtopRequestClass);
                                            RposedHelpers.callMethod(mtopRequest, "setApiName", "mtop.taobao.detail.mainpage.batchget");
                                            RposedHelpers.callMethod(mtopRequest, "setVersion", "1.0");
                                            JSONObject jsonObject = new JSONObject();
                                            jsonObject.put("itemNumIds", itemNumIds);
                                            jsonObject.put("lowDevice", "m");
                                            jsonObject.put("detail_v", "3.3.2");
                                            jsonObject.put("exParams", "{}");
                                            RposedHelpers.callMethod(mtopRequest, "setData", jsonObject.toJSONString());
                                            Class<?> remoteBusinessClass = RposedHelpers.findClass("com.taobao.tao.remotebusiness.RemoteBusiness", lpparam.classLoader);
                                            Object build = RposedHelpers.callStaticMethod(remoteBusinessClass, "build", mtopRequest, ttid);
                                            Class<?> IRemoteBaseListenerClass = RposedHelpers.findClass("com.taobao.tao.remotebusiness.IRemoteBaseListener", lpparam.classLoader);
                                            Object IRemoteListener = Proxy.newProxyInstance(IRemoteBaseListenerClass.getClassLoader(), new Class[]{IRemoteBaseListenerClass}, new InvocationHandler() {
                                                @Override
                                                public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                                                    if (method.getName().equals("onSystemError") || method.getName().equals("onError")) {
                                                        String string = JSON.toJSONString(objects[1]);
                                                        sekiroResponse.success(string);
                                                        return null;
                                                    }
                                                    if (method.getName().equals("onSuccess")) {
                                                        byte[] bytes = (byte[]) RposedHelpers.callMethod(objects[1], "getBytedata");
                                                        JSONObject jsonObject = JSONObject.parseObject(new String(bytes));
                                                        sekiroResponse.success(jsonObject);
                                                        return null;
                                                    }
                                                    return null;
                                                }
                                            });
                                            RposedHelpers.callMethod(build, "registerListener", IRemoteListener);
                                            RposedHelpers.callMethod(build, "setUnitStrategy", UnitStrategy.UNIT_TRADE.toString());
                                            RposedHelpers.callMethod(build, "setBizId", 9998);
                                            RposedHelpers.callMethod(build, "startRequest");
                                        }
                                    });
                        }
                    }).start();
                    Log.i(TAG, "server is running...");
                }
            }
            if(hookSpdy && lpparam.packageName.equals("com.taobao.taobao")){
                RposedHelpers.findAndHookMethod("mtopsdk.mtop.global.SwitchConfig", lpparam.classLoader, "isGlobalSpdySwitchOpen", new RC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(RC_MethodHook.MethodHookParam param) throws Throwable {
                        param.setResult(false);
                    }
                });
                hookSpdy = !hookSpdy;
            }
        }
    }

    private String getUa(ClassLoader classLoader){

        Class<?> WXEnvironment = RposedHelpers.findClass("com.taobao.weex.WXEnvironment", classLoader);
        Map<String,String> config = (Map<String, String>) RposedHelpers.callStaticMethod(WXEnvironment, "getConfig");
        return assembleUserAgent(config);

    }
    public static String assembleUserAgent(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append(map.get("sysModel"));
        sb.append("(Android/");
        sb.append(map.get("sysVersion"));
        sb.append(")");
        sb.append(" ");
        sb.append(TextUtils.isEmpty(map.get("appGroup")) ? "" : map.get("appGroup"));
        sb.append("(");
        sb.append(TextUtils.isEmpty(map.get("appName")) ? "" : map.get("appName"));
        sb.append("/");
        sb.append(map.get("appVersion"));
        sb.append(")");
        sb.append(" ");
        return sb.toString();
    }

}
