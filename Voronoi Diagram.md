# Voronoi Diagram
## 執行介面
![](https://i.imgur.com/B1pc7Gr.png)
可以直接利用滑鼠在視窗中點擊，或者下載測試資料
執行分兩種模式
- Run
直接輸出結果 Voronoi diagram
- Step by step
一步一步執行
## 軟體規格書
輸入規格
```
n (資料量)
x_1 y_1 (座標值 皆為浮點數)
...
#可能包含以#開頭的註解，需略過
...
x_n y_n
```
輸出規格，其中皆以lexical order方式排序
邊的話需先排序邊的兩點座標，再進行全部邊的排序
確保`px_i < ex_i` ， `py_i < ey_i`
並且`px_i < px_i+1`，`py_i < ey_i+1`
```
P x_1 y_1 (點的座標值)
P x_2 y_2
...
P x_n y_n
E px_1 py_1 ex_1 ey_1 (一條邊的兩點x,y值)
E px_2 py_2 ex_2 ey_2
...
E px_n py_n ex_n ey_n
```
### 測試資料
[網頁上的(含註解)](https://par.cse.nsysu.edu.tw/~cbyang/course/algo/vd_testdata.in)
[網頁上的(不含註解)](https://par.cse.nsysu.edu.tw/~cbyang/course/algo/vd_testdata_pure.in)
[自訂測試資料](https://drive.google.com/file/d/10zRAdSHZXauczl7s2sFu7Jf5ukqZChoR/view?usp=sharing)
### 功能規格
`Clear`: 清除目前所有資料

`Open file`: 可以開啟輸入/輸出檔

`Save file`: 儲存目前執行的狀態

`Run`: 直接畫出所有結果

`Step by step`: 一步一步畫出目前的圖
### 介面規格
介面大小為1024*768
![](https://i.imgur.com/tq0HEsb.png)

### 環境
- OS: Windows 10
- Language: Kotlin
- Intellij idea
- JDK: Java 1.8
- Compile to exe and jar

## 軟體說明
1. 直接執行[程式](https://drive.google.com/file/d/1gscLw7B6Cim9QN2tq_hLTZoxyvOgxd-D/view?usp=sharing)即可


## 程式設計說明
主要是利用Canvas畫圖
1. 當使用者在畫布上按下點後會儲存點座標
2. 按下Run 檢測目前座標是兩點還是三點
3. 若為兩點直接計算中點與法向量，將中點往法向量兩邊延伸即可
4. 若為三點，先判斷是否共線，若共線，點兩兩一組類似執行步驟3即可
5. 若沒有共線，先計算外心，然後點兩兩一組計算中點、法向量，從外心往法向量方向延伸畫邊即為Vonoroi Diagram。

## 軟體測試與實驗結果
環境： `Windows 10 64bit`

硬體: 

CPU:`AMD R5-3600`

RAM:`3200Hz 32GB`

顯卡:`RTX 2070`

MB:`TUF B450 PRO Gaming`

編譯器: `Intellij idea Ultimate`

語言: `Kotlin`

Framework: `Tornadofx`

目前可以執行

```
#雙點測試
2
289 290
342 541
#雙點測試 水平
#2
#200 200
#400 200
#雙點測試 垂直
#2
#200 200
#200 400
#雙點測試 重覆
2
200 200
200 200

#三點測試 水平
3
200 200
300 200
400 200
#三點測試 垂直
3
200 200
200 300
200 400
#三點測試 直角三角形
3
200 200
300 200
200 300
#三點測試 銳角三角形
3
147 190
164 361
283 233
#三點測試 鈍角三角形
3
398 93
233 263
345 197
```


## 附錄
[程式](https://drive.google.com/file/d/1gscLw7B6Cim9QN2tq_hLTZoxyvOgxd-D/view?usp=sharing)
[網頁上的測資(含註解)](https://par.cse.nsysu.edu.tw/~cbyang/course/algo/vd_testdata.in)
[網頁上的測資(不含註解)](https://par.cse.nsysu.edu.tw/~cbyang/course/algo/vd_testdata_pure.in)
[自訂測試資料](https://drive.google.com/file/d/10zRAdSHZXauczl7s2sFu7Jf5ukqZChoR/view?usp=sharing)