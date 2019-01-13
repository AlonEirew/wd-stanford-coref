# wd-stanford-coref
Extract Stanford Within-Document(WD) Coref from corpus data that is already tokenized (Skipping CoreNLP pipeline tokenization and aligning coref output tokens with corpus tokens)

Include implementation of extracting ECB+ WD

Pre-Requirements
--
- Java 1.8
- Gradle
- ECB+ corpus for running WD from ECB+ (<a href="http://www.newsreader-project.eu/results/data/the-ecb-corpus/">Download ECB+</a>)

Build And Run Within Doc coref from ECB+
--
* Clone the repo
* From command line navigate to project root directory and run:
    
    
    ./gradlew clean buildCorefJar
    
*Should get a message saying: `BUILD SUCCESSFUL in 25s`*
* Then run command

    
    java --add-modules java.se.ee -jar build/libs/stanford-coref-1.0-SNAPSHOT.jar -corpus ECB+ -output output/ecb_wd_coref.json


##### Arguments:

* `-corpus`: the path location of corpus folder (eg. ECB+)
* `-output`: file to save the json wd coref into


Experiment with other corpus
--
* Clone the repo
* Inherit `IDataLoader` and create a new `DataLoader` for parsing your corpus (see `EcbDataLoader` for example)
* replace `IDataLoader` in `ExtractStanfordCoref`, `main()` method


Output
--
Output is in a json format, containing a list of within document coreference mentions:

    [
        {
            "coref_chain": "0",
            "doc_id": "36_5ecb.xml",
            "sent_id": 4,
            "tokens_number": [
                1,
                2
            ],
            "tokens_str": "Mr. Blackmore"
        },
        {
            "coref_chain": "0",
            "doc_id": "36_5ecb.xml",
            "sent_id": 4,
            "tokens_number": [
                7
            ],
            "tokens_str": "he"
        },
        .
        .
        .
    ]
    
#### Where:
  
| json field  | Value | comment |
| ------------- | ------------- | ------------- |
| coref_chain | Text | The mention coref cluster id |
| doc_id | Text | the document this mention belong to |
| sent_id | int | Mention original document sentence ID |
| tokens_number | List[int] | Mention span (text phrase as set in tokens_str) original tokens ids |
| tokens_str | String | The mention/span phrase |
