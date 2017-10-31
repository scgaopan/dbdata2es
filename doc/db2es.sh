#!/bin/bash
count=0
rm target.json
touch target.json


while read line;do

((count++))

{
        echo $line >> target.json

        if [ $count -gt 10 ] && [ $((count%2)) -eq 0 ];then
                count=0
                curl -XPOST localhost:9200/_bulk --data-binary @target.json > /dev/null
                rm target.json
                touch target.json
        fi

}

done < $1
echo 'last submit'
curl -XPOST localhost:9200/_bulk --data-binary @target.json > /dev/null
