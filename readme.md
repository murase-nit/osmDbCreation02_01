# 概要
ストローク生成のためのプログラム(stroke_v2)

生成されるストロークのデータ構造は
tk-teraの\tsg\資料\時空間rain2データベース覚書_OSM編.xlsxのstrokeの項目を参照

## ストロークの仕様
* 高速道路は使わない

* あるリンクLaと同じストロークとなるリンクLの条件

* LはLaと接触する

* LとLa道路クラスが同じ

* LとLaのなす角が45度以下

* 上の条件を満たすリンクが複数あるときはなす角が最小のリンクが1つ選ばれる

## 設定
* PostgreSQL,PostGISのドライバをビルドパスに通す

* \src\src\handledb\globalのConfig_createDB.java, Config_raodDB.javaが設定ファイル

* Config_createDB.javaはストロークの保存先の設定
	* Config_createDB.javaはストロークの保存先を指定してください(ローカルのDBなど)．

* Config_raodDB.javaは道路データベースの設定
	* Config_raodDB.javaの設定はrain2があればいじらなくてよい．
