import fasttext

if __name__ == '__main__':
    model = fasttext.train_supervised(input='title_label.train', lr=1.0, epoch=10000, wordNgrams=5)
    model.quantize(input='title_label.train', retrain=True)
    # model.save_model('category_classifier_test.ftz')
    model.save_model('category_classifier.ftz')
