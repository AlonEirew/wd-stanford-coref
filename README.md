# ecb-wd-stanford-coref
Extract Within-Document(WD) Coref from ECB+ corpus using Stanford coref


Pre-Requirements
--
- Java 1.8
- Gradle
- ECB+ corpus (<a href="http://www.newsreader-project.eu/results/data/the-ecb-corpus/">Download ECB+</a>)

Build And Run
--
* Clone the repo
* From command line navigate to project root directory and run:
    
    
    ./gradlew clean customFatJar
    
*Should get a message saying: `BUILD SUCCESSFUL in 7s`*
* Then run command

    
    java --add-modules java.se.ee -jar build/libs/stanford-coref-1.0-SNAPSHOT.jar -ecb ECB+ -output output/ecb_wd_coref.json


##### Arguments:

* `-ecb`: the path location of ECB+ corpus
* `-output`: file to save the json wd coref into

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
