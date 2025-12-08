# Sentinel 鎺у埗鍙?

## 0. 姒傝堪

Sentinel 鎺у埗鍙版槸娴侀噺鎺у埗銆佺啍鏂檷绾ц鍒欑粺涓€閰嶇疆鍜岀鐞嗙殑鍏ュ彛锛屽畠涓虹敤鎴锋彁渚涗簡鏈哄櫒鑷彂鐜般€佺皣鐐归摼璺嚜鍙戠幇銆佺洃鎺с€佽鍒欓厤缃瓑鍔熻兘銆傚湪 Sentinel 鎺у埗鍙颁笂锛屾垜浠彲浠ラ厤缃鍒欏苟瀹炴椂鏌ョ湅娴侀噺鎺у埗鏁堟灉銆?

## 1. 缂栬瘧鍜屽惎鍔?

### 1.1 濡備綍缂栬瘧

浣跨敤濡備笅鍛戒护灏嗕唬鐮佹墦鍖呮垚涓€涓?fat jar:

```bash
mvn clean package
```

### 1.2 濡備綍鍚姩

浣跨敤濡備笅鍛戒护鍚姩缂栬瘧鍚庣殑鎺у埗鍙帮細

```bash
java -Dserver.port=8080 \
-Dcsp.sentinel.dashboard.server=localhost:8080 \
-Dproject.name=sentinel-dashboard \
-jar target/sentinel-dashboard.jar
```

涓婅堪鍛戒护涓垜浠寚瀹氬嚑涓?JVM 鍙傛暟锛屽叾涓?`-Dserver.port=8080` 鏄?Spring Boot 鐨勫弬鏁帮紝
鐢ㄤ簬鎸囧畾 Spring Boot 鏈嶅姟绔惎鍔ㄧ鍙ｄ负 `8080`銆傚叾浣欏嚑涓槸 Sentinel 瀹㈡埛绔殑鍙傛暟銆?

涓轰究浜庢紨绀猴紝鎴戜滑瀵规帶鍒跺彴鏈韩鍔犲叆浜嗘祦閲忔帶鍒跺姛鑳斤紝鍏蜂綋鍋氭硶鏄紩鍏?Sentinel 鎻愪緵鐨?`CommonFilter` 杩欎釜 Servlet Filter銆?
涓婅堪 JVM 鍙傛暟鐨勫惈涔夋槸锛?

 |鍙傛暟|浣滅敤||--------|--------||`-Dcsp.sentinel.dashboard.server=localhost:8080`|鍚?Sentinel 鎺ュ叆绔寚瀹氭帶鍒跺彴鐨勫湴鍧€||`-Dproject.name=sentinel-dashboard`|鍚?Sentinel 鎸囧畾搴旂敤鍚嶇О锛屾瘮濡備笂闈㈠搴旂殑搴旂敤鍚嶇О灏变负 `sentinel-dashboard`|

鍏ㄩ儴鐨勯厤缃」鍙互鍙傝€?[鍚姩閰嶇疆椤规枃妗(https://github.com/alibaba/Sentinel/wiki/%E5%90%AF%E5%8A%A8%E9%85%8D%E7%BD%AE%E9%A1%B9)銆?

缁忚繃涓婅堪閰嶇疆锛屾帶鍒跺彴鍚姩鍚庝細鑷姩鍚戣嚜宸卞彂閫佸績璺炽€傜▼搴忓惎鍔ㄥ悗娴忚鍣ㄨ闂?`localhost:8080` 鍗冲彲璁块棶 Sentinel 鎺у埗鍙般€?

浠?Sentinel 1.6.0 寮€濮嬶紝Sentinel 鎺у埗鍙版敮鎸佺畝鍗曠殑**鐧诲綍**鍔熻兘锛岄粯璁ょ敤鎴峰悕鍜屽瘑鐮侀兘鏄?`sentinel`銆傜敤鎴峰彲浠ラ€氳繃濡備笅鍙傛暟杩涜閰嶇疆锛?

- `-Dsentinel.dashboard.auth.username=sentinel` 鐢ㄤ簬鎸囧畾鎺у埗鍙扮殑鐧诲綍鐢ㄦ埛鍚嶄负 `sentinel`锛?
- `-Dsentinel.dashboard.auth.password=123456` 鐢ㄤ簬鎸囧畾鎺у埗鍙扮殑鐧诲綍瀵嗙爜涓?`123456`锛涘鏋滅渷鐣ヨ繖涓や釜鍙傛暟锛岄粯璁ょ敤鎴峰拰瀵嗙爜鍧囦负 `sentinel`锛?
- `-Dserver.servlet.session.timeout=7200` 鐢ㄤ簬鎸囧畾 Spring Boot 鏈嶅姟绔?session 鐨勮繃鏈熸椂闂达紝濡?`7200` 琛ㄧず 7200 绉掞紱`60m` 琛ㄧず 60 鍒嗛挓锛岄粯璁や负 30 鍒嗛挓锛?

## 2. 瀹㈡埛绔帴鍏?

閫夋嫨鍚堥€傜殑鏂瑰紡鎺ュ叆 Sentinel锛岀劧鍚庡湪搴旂敤鍚姩鏃跺姞鍏?JVM 鍙傛暟 `-Dcsp.sentinel.dashboard.server=consoleIp:port` 鎸囧畾鎺у埗鍙板湴鍧€鍜岀鍙ｃ€?
纭繚瀹㈡埛绔湁璁块棶閲忥紝**Sentinel 浼氬湪瀹㈡埛绔娆¤皟鐢ㄧ殑鏃跺€欒繘琛屽垵濮嬪寲锛屽紑濮嬪悜鎺у埗鍙板彂閫佸績璺冲寘**锛屽皢瀹㈡埛绔撼鍏ュ埌鎺у埗鍙扮殑绠¤緰涔嬩笅銆?

瀹㈡埛绔帴鍏ョ殑璇︾粏姝ラ璇峰弬鑰?[Wiki 鏂囨。](https://github.com/alibaba/Sentinel/wiki/%E6%8E%A7%E5%88%B6%E5%8F%B0#3-%E5%AE%A2%E6%88%B7%E7%AB%AF%E6%8E%A5%E5%85%A5%E6%8E%A7%E5%88%B6%E5%8F%B0)銆?

## 3. 楠岃瘉鏄惁鎺ュ叆鎴愬姛

瀹㈡埛绔纭厤缃苟鍚姩鍚庯紝浼?*鍦ㄥ垵娆¤皟鐢ㄥ悗**涓诲姩鍚戞帶鍒跺彴鍙戦€佸績璺冲寘锛屾眹鎶ヨ嚜宸辩殑瀛樺湪锛?
鎺у埗鍙版敹鍒板鎴风蹇冭烦鍖呬箣鍚庯紝浼氬湪宸︿晶瀵艰埅鏍忎腑鏄剧ず璇ュ鎴风淇℃伅銆傚鏋滄帶鍒跺彴鑳藉鐪嬪埌瀹㈡埛绔殑鏈哄櫒淇℃伅锛屽垯琛ㄦ槑瀹㈡埛绔帴鍏ユ垚鍔熶簡銆?

## 6. 鏋勫缓Docker闀滃儚

```bash
docker build --build-arg SENTINEL_VERSION=1.8.9 -t ${REGISTRY}/sentinel-dashboard:v1.8.9 .
```

*娉ㄦ剰锛歋entinel 鎺у埗鍙扮洰鍓嶄粎鏀寔鍗曟満閮ㄧ讲銆係entinel 鎺у埗鍙伴」鐩彁渚?Sentinel 鍔熻兘鍏ㄩ泦绀轰緥锛屼笉浣滀负寮€绠卞嵆鐢ㄧ殑鐢熶骇鐜鎺у埗鍙帮紝涓嶆彁渚涘畨鍏ㄥ彲闈犱繚闅溿€傝嫢甯屾湜鍦ㄧ敓浜х幆澧冧娇鐢ㄨ鏍规嵁[鏂囨。](https://github.com/alibaba/Sentinel/wiki/%E5%9C%A8%E7%94%9F%E4%BA%A7%E7%8E%AF%E5%A2%83%E4%B8%AD%E4%BD%BF%E7%94%A8-Sentinel)鑷杩涜瀹氬埗鍜屾敼閫犮€?

鏇村锛歔鎺у埗鍙板姛鑳戒粙缁峕(./Sentinel_Dashboard_Feature.md)銆?
