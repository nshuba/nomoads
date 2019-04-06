# NoMoAds
This is a repository for NoMoAds - a system for predicting whether or not network packets contain a
request for an ad. For other details about the project, visit the project
[website](http://athinagroup.eng.uci.edu/projects/nomoads/).

## Citing NoMoAds
If you create a publication (including web pages, papers published by a
third party, and publicly available presentations) using NoMoAds or the 
NoMoAds dataset, please cite the 
[corresponding paper](https://www.petsymposium.org/2018/files/papers/issue4/popets-2018-0035.pdf) 
as follows:

```
@article{shuba2018nomoads,
  title={{NoMoAds: Effective and Efficient Cross-App Mobile Ad-Blocking}},
  author={Shuba, Anastasia and Markopoulou, Athina and Shafiq, Zubair},
  journal={Proceedings on Privacy Enhancing Technologies},
  volume={2018},
  number={4},
  year={2018},
  publisher={De Gruyter Open}
}
```

We also encourage you to provide us (<nomoads.uci@gmail.com>) with a
link to your publication. We use this information in reports to our
funding agencies.

## Quick Start
### Prerequisites
* Python 2.7
    - tldextract module (to install run: 'pip install tldextract')
* JRE 1.8
* **Optional**: IntelliJ or Android Studio

### Download the Sample Dataset
* Download the NoMoAds dataset from
[our website](http://athinagroup.eng.uci.edu/projects/nomoads/data/).

* Unzip the contents and place them in a folder of your choosing.
Throughout the document we will refer to this folder as `DATA_ROOT`.
Your folder structure should look as follows:
```
DATA_ROOT
    --> raw_data/
    --> apps_sorted.csv
```

### Download the NoMoAds Source Code
* Download the NoMoAds source code from GitHub. For instance:
```
git clone https://github.com/UCI-Networking-Group/nomoads.git
```

* Throughout the document we will refer to the root folder of the source
code as `CODE_ROOT`

### Taking NoMoAds for a Test Run
* NoMoAds has various modes of operation that are controlled by a
configuration file. A sample configuration file is available at
`CODE_ROOT/config/config.cfg`. Open the sample configuration file in
your favoriate editor and change the `dataRootDir` option to
point to your `DATA_ROOT`. For instance, if your `DATA_ROOT` is located
in `/home/user_a/DATA_ROOT`, the config file should contain the
following:
```
dataRootDir=/home/user_a/DATA_ROOT
```

* Note that if you are on Windows, you can specify the path as
`C:\\Users\\user_a\\DATA_ROOT`

* Now prepare the training data:
```
cd CODE_ROOT/scripts
./prepare_training_data.py config.cfg
```

* The above command will organize the data and will keep it in
`DATA_ROOT/tr_data_per_package_responsible`. Note that in the above
command you can pass a different configuration file, so long as it is
kept in `CODE_ROOT/config/`.

* Now train a classifier and evaluate it:
```
cd CODE_ROOT
./gradlew build
./gradlew run
```

### Extracting Classification Results
* Since the sample configuration file sets
`trainerClass=NetworkLayerTrainer`, the classifiers, logs, and the
results will be saved in `DATA_ROOT/NetworkLayerTrainer`.

* In the sample configuration file, the `binSize` is set to a number
equal to the number of apps in our dataset. This forces NoMoAds to do
a 5-fold packet-based cross-validation. The results of this
cross-validation are saved in
`DATA_ROOT/NetworkLayerTrainer/logs/eval_TIMESTAMP.json`.

* For a more readable format run the following:
```
cd CODE_ROOT/
./scripts/json_pretty_print.py DATA_ROOT/NetworkLayerTrainer/logs/eval_TIMESTAMP.json
```

* Now you can easily read the results in `eval_TIMESTAMP.json`!

## Detailed Overview
If you wish to use NoMoAds for more complex experiments and/or to expand
its capabilities, follow the references below as needed.

### Configuration Settings
The sample config file (`CODE_ROOT/config/config.cfg`) contains some
default settings, but if you wish to change them, follow the guide below.

`dataRootDir` - must point to your `DATA_ROOT` directory.

`trainerClass` - specifies which Trainer class to use. Must be the name
of one of the Trainer class children (e.g. `UrlHeadersPiiAdsTrainer`)

`classifierType` - specifies how to break the training data. Must be one
of the following:
  * package_responsible - split based on the package responsible for
  fetching the ad.
  * package_name - split based on the package responsible for the HTTP
  connection that fetched the ad. Note that this is not always the same
  as the 'package_responsible.' Sometimes apps use Google apps to fetch
  ads for themselves.
  * domain - split based on the destination domain.

`binSize` - specifies the bin size for per-app (or per-domain)
cross-validation.
  * *App(Domain)-Based Cross-Validation*: If there is a total of 50 apps
(or domains), and you set the binSize to 5, then the data will be split
into 10 bins, each containing 5, randomly selected apps (or domains).
Training will be done on 9 bins, and
testing on the remaining bin. The procedure will be
repeated until all bins were tested once.
  * *Packet-Based Cross-Validation*:
  If you set the binSize to be equal to or higher than the number of
  apps/domains, then packet-based cross-validation will be performed
  instead.

`stopwordConfig` - specifies were to load "stop words" from. Stop words
are words that are not to be used as features. For instance, frequently
occuring strings or version numbers. You can use the default list
`config/stop_words.txt` or create your own.

### Java Code
The Javadoc sits in the `docs` directory of the repo and is also available
in web form [here](https://uci-networking-group.github.io/nomoads/).

### Python Scripts
The following scripts are in `CODE_ROOT/scripts`, see the description
in each file for more details on how to use them:

#### Main Scripts

`evaluate_results.py` - used to calculate various ML metrics from the
`DATA_ROOT/<Trainer>/results` folder. See the
[Configuration Settings](#configuration-settings) for details on when
this script should be used.

`prepare_training_data.py` - used to prepare the training data (as
discussed in
[Taking NoMoAds for a Test Run](#taking-nomoads-for-a-test-run))

#### Utility Scripts

`json_pretty_print.py` - convenience script to convert a file (or all
files within a directory) from a one-line JSON to a formatted JSON.

`visualize_tree.py` - convenience script for converting a DOT file to
a PNG to better visualize the tree classifiers.

`settings.py` - parses the configuration file and sets various variables
for other scripts to use.

`utils.py` - contains various global variables and utility methods
used by other scripts.

## Acknowledgements
* [ReCon](https://github.com/Eyasics/recon)
