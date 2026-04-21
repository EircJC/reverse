### 这是结合了德州扑克和炉石传说的一个小游戏

在打德扑的时候可以使用技能卡，技能卡分3种类别  
1:主动; 2:防御； 3:陷阱  
而使用技能卡时需要消耗能量，每个玩家在每局开始时会获得初始能量，每张卡牌随着能力不同所消耗的能量也不同  

![C2kmX0.png](https://i.imgs.ovh/2025/12/12/C2kmX0.png)

启动ServerApplication和WebApplication后，就可以在浏览器中访问游戏了  

提供默认2个账号：jiangchao/123456；jc/123456，分别打开2个浏览器登录试试吧  
http://localhost:8585/texasPage/texasIndex
![C21cbF.png](https://i.imgs.ovh/2026/04/21/Z1va3g.png)  

主动技能使用

![C21Z4X.png](https://i.imgs.ovh/2026/04/21/Z1vcU0.png)

### 数据库init脚本
[texas_init.sql](texas_init.sql)

### 配置
reverse-server项目里面需要注意修改local/application.properties文件，里面有一些默认配置，比如端口号、数据库连接等  
其中redis需要本地或远程连接一个
当前项目用到的JDK版本为1.8

### 成员协作

希望能有感兴趣的朋友一起来完善这个项目
