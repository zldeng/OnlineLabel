#OnlineLabel

简介
---

OnlineLabel是基于Perceptron训练算法的分词、词性标注工具。Perceptron算法原理可参照Michael Collins2002年《Discriminative Training Methods for Hidden Markov Models：Theory and Experiments with Perceptron Algorithms》论文。

目前系统提供了分词、词性标注训练和测试功能。

系统支持多线程测试，测试时可通过thread参数来设置线程数。该参数是可选的，默认设置为单线程。

系统在训练时可使用多线程来进行并行训练，但是当整个训练语料规模比较小时，并行训练性能可能会比单独串行训练略偏低。该参数是可选的，默认设置为单线程。

并行训练的实现参照Ryan McDonald等人2009年NAACL论文《Distributed Training Strategies for the Structured Perceptron》。

单线程训练时使用Average Perceptron算法，多线程并行训练时各线程直接使用Structure Perceptron，不对参数进行Average。实验结果表明并行训练使用Structure Perceptron性能不低于使用Average Perceptron。

在训练时需要提供开发集。在训练过程中会保存每轮迭代产生的模型，并使用开发集对模型进行性能评价。用户可根据训练的log文件件选择性能最优的模型。当然保存每轮迭代的模型造成了训练时间的增加。	

从命令行进行分词训练和测试命令可参考目录下的seg.train.sh和seg.test.sh文件

从命令行进行词性标注训练和测试命令可参考目录下的pos.train.sh和pos.test.sh文件

目前系统提供了模型压缩功能，在训练时通过设定模型特征删除比例来控制删除的特征数量。该参数是可选的，默认对模型不进行压缩。特征删除比例通过参数“-compress”选项进行设置，具体使用可参考pos.train.sh和seg.train.sh文件。

###注意：所有输入输出文件均使用UTF-8编码

文件说明：
---

###data目录：

（1）pos

* conll06.pos.dic：conll06训练语料中抽取的词性标注词典，词典获取的方法是保存训练语料中出现次数大于等于3次的词语及相关词性。
* dev.conll06.pos.gold：conll06语料词性标注开发集gold文件
* test.conll06.seg：conll06语料词性标注测试集文件
* test.conll06.pos：conll06语料词性标注测试集gold文件
* pos.tran.sample：词性标注训练语料样例

（2）seg

* pku_training.dic.utf8： PKU训练语料词典。词典内容为训练语料中统计词频大于等于3的词语。
* pku.test.gold：PKU语料测试集gold文件
* pku.test：PKU语料测试集文件
* seg.train.sample：分词训练语料样例
	
###lib目录：

包含系统引用的jar包 

###config目录：

存放相关的配置文件。目前只包含log4j的配置文件。

###log目录：

存放系统运行的log文件。log文件的文件名可在config目录下的配置文件中进行更改。

词性标注使用的特征：
---

###词语的n_gram特征：	

* w_i (i = -2,-1,0,1,2)
* w_i,w_i+1 (i = -1,0)
* w_-1,w_1

###词边界特征：

* last_char(w_-1)w_0
* first_char(w_0)w_1
* 其中first_char和last_char表示词语的第一个和最后一个字

###词前后缀信息：

* first_char(w_0)last_char(w_0)
* prefix(w_0,i) (i =1,2,3)
* suffix(w_0,i) (i = 1,2,3)
* 其中prefix代表词长度为i的前缀，suffix代表词长度为i的后缀

###词长度信息：

* len(w_0)。词的长度大于五的时候，统一使用五表示

###词典信息：

* postag_lexicon(w_0)。表示词语在词典中的候选词性

###叠字信息：

* 词语中每一个字和词语中的第一个字的组合
* 词语中的每一个字和词语的最后一个字的组合
* 词语中的第i个字和第i+1个字是否相同
* 词语中的第i个字和第i+2个字是否相同

###词语类别信息：

* digit，letter，punctuation以及other

分词使用的特征：
---

###字符n_gram特征：

* c_i (i = -2,-1,0,1,2)
* c_i,c_i+1 (i = -2,-1,0,1)
* c_i,c_i+2 (i = -2,-1,0)
* c_i,c_i+1,c_i+2 (i = -1)

###叠字信息：

* dup(c_i,c_i+1) ：c_i和c_i+1是否是相同字
* dup(c_i,c_i+2) ：c_i和c_i+2是否是相同字
* chartype(c_0) ：c_0的字符类别，包括字母、标点、数字和其他
* chartype(c_0)chartype(c_1)chartype(c_2) ：字符类别trigram
* prefix(c_0,D) ：以c_0开始的在词典D中的最长前缀的长度
* middle(c_0,D) ：c_0位于中间的存在于词典中的最长子串的长度
* suffix(c_0,D) ：以c_0结束的存在于词典中的最长后缀的长度

词典的使用：
---

在使用分词和词性标注测试的过程中，用户均可自行的更改词典内容。只需要添加内容时按照系统提供的词典文件的格式添加即可。

在其他程序中使用分词、词性标注功能：
---

在其他程序中使用分词、词性标注功能的方法可参照src/sample/Test.java文件。

模型文件的获取：
---
由于版权限制，本系统不提供训练语料。但是提供使用各种语料训练好的模型。
由于github在clone时速度的限制，因此将模型文件保存在SCIR服务器上，如果需要可到服务器下载，在服务器上同时还提供了从各个训练语料中提取的词典文件。

###分词：

分词分别使用了PKU、CTB5.0以及人民日报1998年1-6月份语料训练模型。CTB5.0和PKU均是按照常使用的划分标准进行数据划分，人民日报使用2-6月份训练。

###分词模型下载地址：

* [PKU](http://ir.hit.edu.cn/~zldeng/word_segment_data/pku-seg.zip)
* [CTB5.0](http://ir.hit.edu.cn/~zldeng/word_segment_data/ctb5.0-seg.zip)
* [人民日报](http://ir.hit.edu.cn/~zldeng/word_segment_data/peopleDaily1998-seg.zip)

###分词模型性能：

* PKU性能：P： 94.6% R： 94.8% F：94.7%
* CTB5.0性能：开发集：P： 94.38% R：94.62% F： 94.50%	测试集：P：96.64% R：97.71%  F：97.18%
* 人民日报使用1月份语料测试：P：97.10% R：97.58 F：97.34%

###词性标注：

词性标注使用了CTB5.0、conll06以及人民日报1998年1-6月份语料训练模型，CTB5.0和conll6根据常用划分方式进行数据划分，人民日报使用2-6月份训练。

###词性标注模型下载地址：

* [conll06](http://ir.hit.edu.cn/~zldeng/POS_Tagger_data/conll06-pos.zip)
* [CTB5.0](http://ir.hit.edu.cn/~zldeng/POS_Tagger_data/ctb5.0-pos.zip)
* [人民日报](http://ir.hit.edu.cn/~zldeng/POS_Tagger_data/peopleDaily1998-pos.zip)

###词性标注模型性能：	

* conll06性能：开发集：94.3%，测试集：93.7%
* CTB5.0性能：开发集：95.19% 测试集：94.71%
* 人民日报使用1月份语料进行测试，性能为：97.98%
