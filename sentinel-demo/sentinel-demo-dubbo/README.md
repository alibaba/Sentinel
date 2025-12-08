# Sentinel Dubbo Demo

Sentinel 鎻愪緵浜嗕笌 Dubbo 鏁村悎鐨勬ā鍧?- Sentinel Dubbo Adapter锛屼富瑕佸寘鎷拡瀵?Service Provider 鍜?Service Consumer 瀹炵幇鐨?Filter銆備娇鐢ㄦ椂鐢ㄦ埛鍙渶寮曞叆浠ヤ笅妯″潡锛堜互 Maven 涓轰緥锛夛細

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-dubbo-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

寮曞叆姝や緷璧栧悗锛孌ubbo 鐨勬湇鍔℃帴鍙ｅ拰鏂规硶锛堝寘鎷皟鐢ㄧ鍜屾湇鍔＄锛夊氨浼氭垚涓?Sentinel 涓殑璧勬簮锛屽湪閰嶇疆浜嗚鍒欏悗灏卞彲浠ヨ嚜鍔ㄤ韩鍙楀埌 Sentinel 鐨勯槻鎶よ兘鍔涖€?

> **娉細鑻ュ笇鏈涙帴鍏?Dashboard锛岃鍙傝€冨悗闈㈡帴鍏ユ帶鍒跺彴鐨勬楠ゃ€傚彧寮曞叆 Sentinel Dubbo Adapter 鏃犳硶鎺ュ叆鎺у埗鍙帮紒**

鑻ヤ笉甯屾湜寮€鍚?Sentinel Dubbo Adapter 涓殑鏌愪釜 Filter锛屽彲浠ユ墜鍔ㄥ叧闂搴旂殑 Filter锛屾瘮濡傦細

```java
@Bean
public ConsumerConfig consumerConfig() {
    ConsumerConfig consumerConfig = new ConsumerConfig();
    consumerConfig.setFilter("-sentinel.dubbo.consumer.filter");
    return consumerConfig;
}
```

鎴戜滑鎻愪緵浜嗗嚑涓叿浣撶殑 Demo 鏉ュ垎鍒紨绀?Provider 鍜?Consumer 鐨勯檺娴佸満鏅€?

## Service Provider

Service Provider 鐢ㄤ簬鍚戝鐣屾彁渚涙湇鍔★紝澶勭悊鍚勪釜娑堣垂鑰呯殑璋冪敤璇锋眰銆備负浜嗕繚鎶?Provider 涓嶈婵€澧炵殑娴侀噺鎷栧灝褰卞搷绋冲畾鎬э紝鍙互缁?Provider 閰嶇疆 **QPS 妯″紡**鐨勯檺娴侊紝杩欐牱褰撴瘡绉掔殑璇锋眰閲忚秴杩囪瀹氱殑闃堝€兼椂浼氳嚜鍔ㄦ嫆缁濆鐨勮姹傘€傞檺娴佺矑搴﹀彲浠ユ槸鏈嶅姟鎺ュ彛鍜屾湇鍔℃柟娉曚袱绉嶇矑搴︺€傝嫢甯屾湜鏁翠釜鏈嶅姟鎺ュ彛鐨?QPS 涓嶈秴杩囦竴瀹氭暟鍊硷紝鍒欏彲浠ヤ负瀵瑰簲鏈嶅姟鎺ュ彛璧勬簮锛坮esourceName 涓?*鎺ュ彛鍏ㄩ檺瀹氬悕**锛夐厤缃?QPS 闃堝€硷紱鑻ュ笇鏈涙湇鍔＄殑鏌愪釜鏂规硶鐨?QPS 涓嶈秴杩囦竴瀹氭暟鍊硷紝鍒欏彲浠ヤ负瀵瑰簲鏈嶅姟鏂规硶璧勬簮锛坮esourceName 涓?*鎺ュ彛鍏ㄩ檺瀹氬悕:鏂规硶绛惧悕**锛夐厤缃?QPS 闃堝€笺€傛湁鍏抽厤缃鎯呰鍙傝€?[娴侀噺鎺у埗 | Sentinel](https://github.com/alibaba/Sentinel/wiki/%E6%B5%81%E9%87%8F%E6%8E%A7%E5%88%B6)銆?

Demo 1 婕旂ず浜嗘闄愭祦鍦烘櫙锛屾垜浠湅涓€涓嬭繖绉嶆ā寮忕殑闄愭祦浜х敓鐨勬晥鏋溿€傚亣璁炬垜浠凡缁忓畾涔変簡鏌愪釜鏈嶅姟鎺ュ彛 `com.alibaba.csp.sentinel.demo.dubbo.FooService`锛屽叾涓湁涓€涓柟娉?`sayHello(java.lang.String)`锛孭rovider 绔鏂规硶璁惧畾 QPS 闃堝€间负 10銆傚湪 Consumer 绔湪 1s 涔嬪唴杩炵画鍙戣捣 15 娆¤皟鐢紝鍙互閫氳繃鏃ュ織鏂囦欢鐪嬪埌 Provider 绔闄愭祦銆傛嫤鎴棩蹇楃粺涓€璁板綍鍦?`~/logs/csp/sentinel-block.log` 涓細

```plaintext
2018-07-24 17:13:43|1|com.alibaba.csp.sentinel.demo.dubbo.FooService:sayHello(java.lang.String),FlowException,default,|5,0
```

鍦?Provider 瀵瑰簲鐨?metrics 鏃ュ織涓篃鏈夎褰曪細

```plaintext
1532423623000|2018-07-24 17:13:43|com.alibaba.csp.sentinel.demo.dubbo.FooService|15|0|15|0|3
1532423623000|2018-07-24 17:13:43|com.alibaba.csp.sentinel.demo.dubbo.FooService:sayHello(java.lang.String)|10|5|10|0|0
```

寰堝鍦烘櫙涓嬶紝鏍规嵁**璋冪敤鏂?*鏉ラ檺娴佷篃鏄潪甯搁噸瑕佺殑銆傛瘮濡傛湁涓や釜鏈嶅姟 A 鍜?B 閮藉悜 Service Provider 鍙戣捣璋冪敤璇锋眰锛屾垜浠笇鏈涘彧瀵规潵鑷湇鍔?B 鐨勮姹傝繘琛岄檺娴侊紝鍒欏彲浠ヨ缃檺娴佽鍒欑殑 `limitApp` 涓烘湇鍔?B 鐨勫悕绉般€係entinel Dubbo Adapter 浼氳嚜鍔ㄨВ鏋?Dubbo 娑堣垂鑰咃紙璋冪敤鏂癸級鐨?application name 浣滀负璋冪敤鏂瑰悕绉帮紙`origin`锛夛紝鍦ㄨ繘琛岃祫婧愪繚鎶ょ殑鏃跺€欓兘浼氬甫涓婅皟鐢ㄦ柟鍚嶇О銆傝嫢闄愭祦瑙勫垯鏈厤缃皟鐢ㄦ柟锛坄default`锛夛紝鍒欒闄愭祦瑙勫垯瀵规墍鏈夎皟鐢ㄦ柟鐢熸晥銆傝嫢闄愭祦瑙勫垯閰嶇疆浜嗚皟鐢ㄦ柟鍒欓檺娴佽鍒欏皢浠呭鎸囧畾璋冪敤鏂圭敓鏁堛€?

> 娉細Dubbo 榛樿閫氫俊涓嶆惡甯﹀绔?application name 淇℃伅锛屽洜姝ら渶瑕佸紑鍙戣€呭湪璋冪敤绔墜鍔ㄥ皢 application name 缃叆 attachment 涓紝provider 绔繘琛岀浉搴旂殑瑙ｆ瀽銆係entinel Dubbo Adapter 瀹炵幇浜嗕竴涓?Filter 鐢ㄤ簬鑷姩浠?consumer 绔悜 provider 绔€忎紶 application name銆傝嫢璋冪敤绔湭寮曞叆 Sentinel Dubbo Adapter锛屽張甯屾湜鏍规嵁璋冪敤绔檺娴侊紝鍙互鍦ㄨ皟鐢ㄧ鎵嬪姩灏?application name 缃叆 attachment 涓紝key 涓?`dubboApplication`銆?

鍦ㄩ檺娴佹棩蹇椾腑浼氫篃浼氳褰曡皟鐢ㄦ柟鐨勫悕绉帮紝濡傦細

```plaintext
2018-07-25 16:26:48|1|com.alibaba.csp.sentinel.demo.dubbo.FooService:sayHello(java.lang.String),FlowException,default,demo-consumer|5,0
```

鍏朵腑鏃ュ織涓殑 `demo-consumer` 鍗充负璋冪敤鏂瑰悕绉般€?

## Service Consumer

> 瀵规湇鍔℃秷璐规柟鐨勬祦閲忔帶鍒跺彲鍒嗕负**鎺у埗骞跺彂绾跨▼鏁?*鍜?*鏈嶅姟闄嶇骇**涓や釜缁村害銆?

### 骞跺彂绾跨▼鏁伴檺娴?

Service Consumer 浣滀负瀹㈡埛绔幓璋冪敤杩滅▼鏈嶅姟銆傛瘡涓€涓湇鍔￠兘鍙兘浼氫緷璧栧嚑涓笅娓告湇鍔★紝鑻ユ煇涓湇鍔?A 渚濊禆鐨勪笅娓告湇鍔?B 鍑虹幇浜嗕笉绋冲畾鐨勬儏鍐碉紝鏈嶅姟 A 璇锋眰鏈嶅姟 B 鐨勫搷搴旀椂闂村彉闀匡紝浠庤€屾湇鍔?A 璋冪敤鏈嶅姟 B 鐨勭嚎绋嬪氨浼氫骇鐢熷爢绉紝鏈€缁堝彲鑳借€楀敖鏈嶅姟 A 鐨勭嚎绋嬫暟銆傛垜浠€氳繃鐢ㄥ苟鍙戠嚎绋嬫暟鏉ユ帶鍒跺涓嬫父鏈嶅姟 B 鐨勮闂紝鏉ヤ繚璇佷笅娓告湇鍔′笉鍙潬鐨勬椂鍊欙紝涓嶄細鎷栧灝鏈嶅姟鑷韩銆傚熀浜庤繖绉嶅満鏅紝鎺ㄨ崘缁?Consumer 閰嶇疆**绾跨▼鏁版ā寮?*鐨勯檺娴侊紝鏉ヤ繚璇佽嚜韬笉琚笉绋冲畾鏈嶅姟鎵€褰卞搷銆傞檺娴佺矑搴﹀悓鏍峰彲浠ユ槸鏈嶅姟鎺ュ彛鍜屾湇鍔℃柟娉曚袱绉嶇矑搴︺€?

閲囩敤鍩轰簬绾跨▼鏁扮殑闄愭祦妯″紡鍚庯紝鎴戜滑涓嶉渶瑕佸啀鏄惧紡鍦板幓杩涜绾跨▼姹犻殧绂伙紝Sentinel 浼氭帶鍒惰祫婧愮殑绾跨▼鏁帮紝瓒呭嚭鐨勮姹傜洿鎺ユ嫆缁濓紝鐩村埌鍫嗙Н鐨勭嚎绋嬪鐞嗗畬鎴愩€?

Demo 2 婕旂ず浜嗘闄愭祦鍦烘櫙锛屾垜浠湅涓€涓嬭繖绉嶆ā寮忕殑鏁堟灉銆傚亣璁惧綋鍓嶆湇鍔?A 渚濊禆涓や釜杩滅▼鏈嶅姟鏂规硶 `sayHello(java.lang.String)` 鍜?`doAnother()`銆傚墠鑰呰繙绋嬭皟鐢ㄧ殑鍝嶅簲鏃堕棿 涓?1s-1.5s涔嬮棿锛屽悗鑰?RT 闈炲父灏忥紙30 ms 宸﹀彸锛夈€傛湇鍔?A 绔涓や釜杩滅▼鏂规硶 thread count 涓?5銆傜劧鍚庢瘡闅?50 ms 宸﹀彸鍚戠嚎绋嬫睜鎶曞叆涓や釜浠诲姟锛屼綔涓烘秷璐硅€呭垎鍒繙绋嬭皟鐢ㄥ搴旀柟娉曪紝鎸佺画 10 娆°€傚彲浠ョ湅鍒?`sayHello` 鏂规硶琚檺娴?5 娆★紝鍥犱负鍚庨潰璋冪敤鐨勬椂鍊欏墠闈㈢殑杩滅▼璋冪敤杩樻湭杩斿洖锛圧T 楂橈級锛涜€?`doAnother()` 璋冪敤鍒欎笉鍙楀奖鍝嶃€傜嚎绋嬫暟鐩秴鍑烘椂蹇€熷け璐ヨ兘澶熸湁鏁堝湴闃叉鑷繁琚參璋冪敤鎵€褰卞搷銆?

### 鏈嶅姟闄嶇骇

褰撴湇鍔′緷璧栦簬澶氫釜涓嬫父鏈嶅姟锛岃€屾煇涓笅娓告湇鍔¤皟鐢ㄩ潪甯告參鏃讹紝浼氫弗閲嶅奖鍝嶅綋鍓嶆湇鍔＄殑璋冪敤銆傝繖閲屾垜浠彲浠ュ埄鐢?Sentinel 鐔旀柇闄嶇骇鐨勫姛鑳斤紝涓鸿皟鐢ㄧ閰嶇疆鍩轰簬骞冲潎 RT 鐨刐闄嶇骇瑙勫垯](https://github.com/alibaba/Sentinel/wiki/%E7%86%94%E6%96%AD%E9%99%8D%E7%BA%A7)銆傝繖鏍峰綋璋冪敤閾捐矾涓煇涓湇鍔¤皟鐢ㄧ殑骞冲潎 RT 鍗囬珮锛屽湪涓€瀹氱殑娆℃暟鍐呰秴杩囬厤缃殑 RT 闃堝€硷紝Sentinel 灏变細瀵规璋冪敤璧勬簮杩涜闄嶇骇鎿嶄綔锛屾帴涓嬫潵鐨勮皟鐢ㄩ兘浼氱珛鍒绘嫆缁濓紝鐩村埌杩囦簡涓€娈佃瀹氱殑鏃堕棿鍚庢墠鎭㈠锛屼粠鑰屼繚鎶ゆ湇鍔′笉琚皟鐢ㄧ鐭澘鎵€褰卞搷銆傚悓鏃跺彲浠ラ厤鍚?fallback 鍔熻兘浣跨敤锛屽湪琚檷绾х殑鏃跺€欐彁渚涚浉搴旂殑澶勭悊閫昏緫銆?

## Fallback

浠?0.1.1 鐗堟湰寮€濮嬶紝Sentinel Dubbo Adapter 杩樻敮鎸侀厤缃叏灞€鐨?fallback 鍑芥暟锛屽彲浠ュ湪 Dubbo 鏈嶅姟琚檺娴?闄嶇骇/璐熻浇淇濇姢鐨勬椂鍊欒繘琛岀浉搴旂殑 fallback 澶勭悊銆傜敤鎴峰彧闇€瑕佸疄鐜拌嚜瀹氫箟鐨?[`DubboFallback`](https://github.com/alibaba/Sentinel/blob/master/sentinel-adapter/sentinel-dubbo-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/dubbo/fallback/DubboFallback.java) 鎺ュ彛锛屽苟閫氳繃 `DubboFallbackRegistry` 娉ㄥ唽鍗冲彲銆傞粯璁ゆ儏鍐典細鐩存帴灏?`BlockException` 鍖呰鍚庢姏鍑恒€傚悓鏃讹紝鎴戜滑杩樺彲浠ラ厤鍚?[Dubbo 鐨?fallback 鏈哄埗](http://dubbo.apache.org/#!/docs/user/demos/local-mock.md?lang=zh-cn) 鏉ヤ负闄嶇骇鐨勬湇鍔℃彁渚涙浛浠ｇ殑瀹炵幇銆?

Demo 2 鐨?Consumer 绔彁渚涗簡涓€涓畝鍗曠殑 fallback 绀轰緥銆?

## Sentinel Dashboard

Sentinel 杩樻彁渚?API 鐢ㄤ簬鑾峰彇瀹炴椂鐨勭洃鎺т俊鎭紝瀵瑰簲鏂囨。瑙乕姝ゅ](https://github.com/alibaba/Sentinel/wiki/%E5%AE%9E%E6%97%B6%E7%9B%91%E6%8E%A7)銆備负浜嗕究浜庝娇鐢紝Sentinel 杩樻彁渚涗簡涓€涓帶鍒跺彴锛圖ashboard锛夌敤浜庨厤缃鍒欍€佹煡鐪嬬洃鎺с€佹満鍣ㄥ彂鐜扮瓑鍔熻兘銆?

鎺ュ叆 Dashboard 鐨勬楠わ紙**缂轰竴涓嶅彲**锛夛細

1. 鎸夌収 [Sentinel 鎺у埗鍙版枃妗(https://github.com/alibaba/Sentinel/wiki/%E6%8E%A7%E5%88%B6%E5%8F%B0) 鍚姩鎺у埗鍙?
2. 搴旂敤寮曞叆 `sentinel-transport-simple-http` 渚濊禆锛屼互渚挎帶鍒跺彴鍙互鎷夊彇瀵瑰簲搴旂敤鐨勭浉鍏充俊鎭?
3. 缁欏簲鐢ㄦ坊鍔犵浉鍏崇殑鍚姩鍙傛暟锛屽惎鍔ㄥ簲鐢ㄣ€傞渶瑕侀厤缃殑鍙傛暟鏈夛細
   - `-Dcsp.sentinel.api.port`锛氬鎴风鐨?port锛岀敤浜庝笂鎶ョ浉鍏充俊鎭?
   - `-Dcsp.sentinel.dashboard.server`锛氭帶鍒跺彴鐨勫湴鍧€
   - `-Dproject.name`锛氬簲鐢ㄥ悕绉帮紝浼氬湪鎺у埗鍙颁腑鏄剧ず

娉ㄦ剰鏌愪簺鐜涓嬫湰鍦拌繍琛?Dubbo 鏈嶅姟杩橀渶瑕佸姞涓?`-Djava.net.preferIPv4Stack=true` 鍙傛暟銆傛瘮濡?Service Provider 绀轰緥鐨勫惎鍔ㄥ弬鏁帮細

```bash
-Djava.net.preferIPv4Stack=true -Dcsp.sentinel.api.port=8720 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=dubbo-provider-demo
```

Service Consumer 绀轰緥鐨勫惎鍔ㄥ弬鏁帮細

```bash
-Djava.net.preferIPv4Stack=true -Dcsp.sentinel.api.port=8721 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=dubbo-consumer-demo
```

杩欐牱鍦ㄥ惎鍔?Service Provider 鍜?Service Consumer 绀轰緥浠ュ悗锛屽氨鍙互鍦?Sentinel 鎺у埗鍙颁腑鎵惧埌鎴戜滑鐨勬湇鍔′簡銆傚彲浠ュ緢鏂逛究鍦板湪鎺у埗鍙颁腑閰嶇疆闄愭祦瑙勫垯锛?

![瑙勫垯閰嶇疆](http://dubbo.incubator.apache.org/img/blog/sentinel-dashboard-view-rules.png)

鎴栬€呮煡鐪嬪疄鏃剁洃鎺ф暟鎹細

![绉掔骇瀹炴椂鐩戞帶](http://dubbo.incubator.apache.org/img/blog/sentinel-dashboard-metrics.png)
