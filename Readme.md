数据导入-从mysql到Elasticsearch

思路：

1 采用JDBC的方式，通过分页读取数据库的全部数据。
2 数据库读取的数据存储成bulk形式的数据，关于bulk需要的文件格式，可以参考这里
3 利用bulk命令分批导入到es中



代码实现，导出json文件

编写shell脚本 10条记录分成一个文件:
#!/bin/bash

count=0
rm target.json
touch target.json


while read line;do

((count++))

{
        echo $line >> target.json

        if [ $count -gt 100000 ] && [ $((count%2)) -eq 0 ];then
                count=0
                curl -XPOST localhost:9200/_bulk --data-binary @target.json > /dev/null
                rm target.json
                touch target.json
        fi

}

done < $1
echo 'last submit'
curl -XPOST localhost:9200/_bulk --data-binary @target.json > /dev/null


最后执行脚本:

sh auto_bulk.sh data.json