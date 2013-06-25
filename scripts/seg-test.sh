nohup java  -cp oonline-label.jar ir.hit.edu.ltp.label.OnlineSeg  -test  \
 -model ./model/pku/pku-seg.model \
 -devFile ./data/pku/pku_test_gold.utf8 \
 -testFile ./data/pku_test.utf8  \
 -result  ./data/pku_test.result  &
