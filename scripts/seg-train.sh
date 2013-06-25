nohup java -Xms15G -Xmx30G -cp online-label.jar ir.hit.edu.ltp.label.OnlineSeg \
 -train  -trainFile ./data/pku/pku_training.utf8  \
 -model ./model/pku/pku-seg.model \
 -devFile ./data/pku/pku_test_gold.utf8 \
 -dicFile ./data/pku/pku_training.dic.utf8  \
 -iterator 20 &
