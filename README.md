# NoMoAds
This is a repository for NoMoAds, with NoMoATS extensions.
NoMoAds is a system for predicting whether or not network packets contain
advertising or tracking requests.
For details about the two projects, please visit
[our website](http://athinagroup.eng.uci.edu/projects/nomoads/).

Below is an outline of this document:
* [Quick Start](#quick-start)
* [Detailed Overview](#detailed-overview)
* [Citing NoMoAds](#citing-nomoads)
* [Acknowledgements](#acknowledgements)

## Quick Start
Below you will find instructions to get you started quickly. We also provide a VM
that has all the prerequisites installed and contains a small subset of our dataset to get you
started even quicker. If you would like to use the VM, go to the
[Using the Provided VM](#using-the-provided-vm) section. Otherwise, start at the
[Prerequisites](#prerequisites) section.

### Using the Provided VM
* Download our Ubuntu 18.04 VMWare image from [here](https://drive.google.com/file/d/1IaDKw66ECPmNSIg4FEnYKw7HpludGFS8/).
The image has all the prerequisites installed and has a subset of our dataset to get you started in no time.
    - Note that the image should also work with Virtual Box if you create a new VM within Virtual Box and pick
    "use existing disk".
* Power on the image. Password is `nomoats`
* Open a terminal and follow the steps below:
  ```
  $ cd ~/nomoads
  $ python scripts/data_prep/prepare_training_data.py config.cfg
  $ ./gradlew build
  $ ./gradlew run
  ```
* The results are ready to view! Skip to the [Viewing Results](#viewing-results) section for
instructions on how to view them. Note that in the `DATA_ROOT` directory is
`/home/nomoads/data_root` on the VM.

### Prerequisites
* **Operating System**: Ubuntu 18.04
* Python 2.7 and pip
    - `$ sudo apt-get install python`
    - `$ sudo apt-get install python-pip`
* tldextract module
    - `$ pip install tldextract`
* Java 8
    - `$ sudo apt-get install openjdk-8-jre`
    - `$ sudo apt-get install openjdk-8-jdk`
* xdot (for viewing decision trees, if desired):
    - `$ sudo apt-get install xdot`
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
your favorite editor and change the `dataRootDir` option to
point to your `DATA_ROOT`. For instance, if your `DATA_ROOT` is located
in `/home/user_a/DATA_ROOT`, the config file should contain the
following:
  ```
  dataRootDir=/home/user_a/DATA_ROOT
  ```

* Now prepare the training data:
  ```
  cd CODE_ROOT/scripts
  ./prepare_training_data.py config.cfg
  ```

* The above command will organize the data and will keep it in
`DATA_ROOT/tr_data_per_package_name`. Note that in the above
command you can pass a different configuration file, so long as it is
kept in `CODE_ROOT/config/`.

* Now train a classifier and evaluate it:
  ```
  cd CODE_ROOT
  ./gradlew build
  ./gradlew run
  ```

### Viewing Results
* Since the sample configuration file sets
`trainerClass=UrlHeadersAdsTrainer`, the results will be saved in the
`DATA_ROOT/UrlHeadersAdsTrainer` folder, and will contain the following items:
    - arff/ - contains `.arff` files used by the Weka library to store data points
    - logs/ - contains various ML metrics provided by Weka during training
    - model/ - contains the decision tree (DT) models
    - results/ - contains a copy of the training dataset with prediction results
    - tree_dot_files/ - contains `.dot` files representing the DTs trained,
    used for visualization
    - treeLabels.json - a JSON represnetation of the trees, used for
    various processing during the training
    
* You can view the produced DTs by opening up any of the `.dot` files in 
`DATA_ROOT/UrlHeadersAdsTrainer/tree_dot_files/`

* Note that all the JSON files are saved in compressed format. For easier viewing,
you can use the `json_pretty_print.py` script to uncompress a file. For example:
  ```
  $ cd CODE_ROOT/
  $ python scripts/utils/json_pretty_print.py DATA_ROOT/UrlHeadersAdsTrainer/treeLabels.json
  ```

* To evaluate how well the classifiers did, you can use the following script:
  ```
  $ cd CODE_ROOT/
  $ python scripts/ml_anal/evaluate_results.py config.cfg
  ```
  The script will print out various ML metrics (F1, accuracy, etc.) on a per-classifier
  bases (per-app in our case) and will also calcualte and print the averages along with
  the standard deviation. The results will also be saved to
  `DATA_ROOT/UrlHeadersAdsTrainer/results_split.csv`

<!---
TODO: add labeling with lists + conversion of results to CSV for SQL
-->

## Detailed Overview
<!---
TODO: update these docs
-->
If you wish to use NoMoAds for more complex experiments and/or to expand
its capabilities, follow the references below as needed.

### Configuration Settings
The sample config file (`CODE_ROOT/config/config.cfg`) contains some
default settings, but if you wish to change them, follow the guide below.

`dataRootDir` - must point to your `DATA_ROOT` directory.

`trainerClass` - specifies which Trainer class to use. Must be the name
of one of the Trainer class children (e.g. `UrlHeadersPiiAdsTrainer`)

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
`DATA_ROOT/<Trainer>/results` folder (as discussed in
[Viewing Results](#viewing-results)).

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

If you used the NoMoATS dataset, please cite the corresponding paper
as follows:

```
@article{shuba2020nomoats,
  title={{NoMoATS: Towards Automatic Detection of Mobile Tracking}},
  author={Shuba, Anastasia and Markopoulou, Athina},
  journal={Proceedings on Privacy Enhancing Technologies},
  volume={2020},
  number={2},
  year={2020},
  publisher={De Gruyter Open}
}
```

We also encourage you to provide us (<nomoads.uci@gmail.com>) with a
link to your publication. We use this information in reports to our
funding agencies.

## Acknowledgements
* [ReCon](https://github.com/Eyasics/recon)
