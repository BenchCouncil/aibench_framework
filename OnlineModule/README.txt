Online module provides personalized searching and recommendations combining traditional machine learning and deep learning technologies. Online module consists of four submodules including search planer, recommender, searcher, ranker, user and product database.

Search Planer is the entrance of online server. Recommender is to analyze the query item and pro- vides personalized recommendation, according to the user information obtained from the user database. Ranker uses the weight returned by Recommender as initial weight, and ranks the scores of products through a personalized L2R neural network. Search Planer returns the searched product information.

We use the Neo4j to store user data, and ElasticSearch to store the index of searcher and ranker, and product data. The workload-generator directory contains the Jmeter binary which is used as our workload generator. And the recommender contains two parts, one is a web service application built-in Flask framework, and the other one is a Tensorflow model serving which is for AI inference.

1. Setup the user info database.
	* Uncompress the neo4j-community-3.5.8-unix.tar.gz in the user-info directory.
	* Start the neo4j using "neo4j-community-3.5.8/bin/neo4j console".
2. Setup the searcher, ranker, and product info database. There are three databases for storing the index of the searcher to mimic different popular distributions of products in the real world. However, for simplicity, we all use ElasticSearch to store the above data, and we can only set up an ElasticSearch instance to store all those data. To scale to multi nodes, you can set up one ElasticSearch cluster for each module.
	* Uncompress the elasticsearch-oss-6.5.2.tar.gz in any directory of product-info, ranker, searcher.
	* Start the ElasticSearch using "elasticsearch-6.5.2/bin/elasticsearch".
	* Set up the index using the init_database.sh in all directory of product-info, ranker, searcher.
3. Setup the recommender-serving.
	* You can use the native TensorFlow model serving and load the model named ranking_weights_model in the recommender-serving directory.
	* Or you can use our provided Dockerfile to build a new docker image, and suppose the image name is tf-serving, you can start the recommender-serving module using "docker run -t --rm -p 8501:8501 -e MODEL_NAME=ranking_weights_model".
4. Setup the recommender-web.
	* For simplicity, we use pipenv to manage the python virtual environment. You can initialize the python environment using "pipenv install" and then activate the environment using "pipenv shell".
	* To start the recommender-web, just using "python recommender.py".
	* Notice: the user info database must be initialized before the recommender-web, otherwise the recommender-web module won't start.
5. Setup the search-planer.
	* We use the Spring framework to build the module and gradle to manage the module. You must set the correct host in the config file: application.properties and factorybean-spring-ctx.xml in the search-planer/src/main/resources/ directory.
	* Simply run "./gradlew bootRun" in the search-planer directory to start the search-planer module.
	* Or you can also package the source code to a bootable jar using "./gradlew bootJar", and the bootable jar will be output in the build/libs directory. Then you can start the module using java -jar theBootableJarName.jar to start the module.
6. Populate the database.
	* Using the benchmark-cli tool to populate the database. The tool can populate user-info, searcher, ranker, and product-info database.
7. Run the workload generator.
	* We use JMeter as our workload generator to send the workload to the online AIBench.

