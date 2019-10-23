FROM tensorflow/serving

COPY ranking_weights_model /models/ranking_weights_model

# The only required piece is the model name in order to differentiate endpoints
ENV MODEL_NAME=ranking_weights_model