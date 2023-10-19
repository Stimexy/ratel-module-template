package ratel.crack.com.taobao.etao;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.virjar.ratel.api.RatelConfig;
import com.virjar.ratel.api.RatelEngine;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.extension.JustTrustMe;
import com.virjar.ratel.api.rposed.IRposedHookLoadPackage;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.api.rposed.callbacks.RC_LoadPackage;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
    private static final String TAG = "mtop_hook";
    private final String CLIENT_ID = Build.MODEL.replace(" ", "") + "_" + Build.VERSION.RELEASE;
    public boolean isStartup = false;


    public enum UnitStrategy {
        UNIT_TRADE,
        UNIT_GUIDE,
        UNIT_NULL;
    }


    @Override
    public void handleLoadPackage(final RC_LoadPackage.LoadPackageParam lpparam) {
        if(lpparam.packageName.equals("com.taobao.live") || lpparam.packageName.equals("com.taobao.etao")) {
            if (lpparam.processName.equals(lpparam.packageName)) {
                JustTrustMe.trustAllCertificate();
                if(!isStartup){
                    Context context = RatelToolKit.sContext;
                    new SekiroClient("tblive", CLIENT_ID, "10.228.84.24", 5612)
                            .setupSekiroRequestInitializer(new SekiroRequestInitializer() {
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
                                                        jsonObject.put("from", RatelToolKit.packageName);
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
                                            RposedHelpers.callMethod(build, "setBizId", 9999);
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
                                            String data = "{\"detail_v\":\"3.3.2\",\"exParams\":\"{\\\"appReqFrom\\\":\\\"detail\\\",\\\"clientCachedTemplateKeys\\\":\\\"[{\\\\\\\"id\\\\\\\":\\\\\\\"1538383035450\\\\\\\",\\\\\\\"version\\\\\\\":\\\\\\\"138\\\\\\\"}]\\\",\\\"container_type\\\":\\\"xdetail\\\",\\\"countryCode\\\":\\\"CN\\\",\\\"cpuCore\\\":\\\"8\\\",\\\"cpuMaxHz\\\":\\\"1766400\\\",\\\"deviceLevel\\\":\\\"low\\\",\\\"dinamic_v3\\\":\\\"true\\\",\\\"finalUltron\\\":\\\"true\\\",\\\"id\\\":\\\"717245427395\\\",\\\"item_id\\\":\\\"717245427395\\\",\\\"latitude\\\":\\\"0\\\",\\\"longitude\\\":\\\"0\\\",\\\"openFrom\\\":\\\"dtaodetail\\\",\\\"osVersion\\\":\\\"28\\\",\\\"phoneType\\\":\\\"GIONEE S10L\\\",\\\"screenHeight\\\":\\\"1920\\\",\\\"screenWidth\\\":\\\"1080\\\",\\\"soVersion\\\":\\\"2.0\\\",\\\"spm\\\":\\\"a2131v.24504623.feed\\\",\\\"spm-cnt\\\":\\\"a2141.7631564\\\",\\\"supportV7\\\":\\\"true\\\",\\\"tl_allow_escape_flag\\\":\\\"false\\\",\\\"ultron2\\\":\\\"true\\\",\\\"utdid\\\":\\\"Y1ijhA/YVAgDAG0pGbjLFa53\\\",\\\"xxc\\\":\\\"diantao\\\"}\",\"itemNumId\":\"717245427395\"}";
                                            JSONObject parse = JSON.parseObject(data);
                                            JSONObject exParams = JSON.parseObject(parse.getString("exParams"));
                                            exParams.put("id",itemId);
                                            exParams.put("item_id",itemId);
                                            exParams.put("utdid",utdid);
                                            parse.put("exParams",JSON.toJSONString(exParams));
                                            parse.put("itemNumId",itemId);
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
                                                        jsonObject.put("from", RatelToolKit.packageName);
                                                        sekiroResponse.success(jsonObject);
                                                        return null;
                                                    }
                                                    return null;
                                                }
                                            });
                                            RposedHelpers.callMethod(build, "registerListener", IRemoteListener);
                                            RposedHelpers.callMethod(build, "setUnitStrategy", UnitStrategy.UNIT_TRADE.toString());
                                            RposedHelpers.callMethod(build, "setBizId",9998);
                                            RposedHelpers.callMethod(build, "startRequest");
                                        }
                                    });
                                }
                            }).start();
                    Log.i(TAG, context.getPackageName()+":sekiro server start");
                }
            }
        }
    }
}
