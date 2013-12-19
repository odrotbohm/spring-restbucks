HAL.Views.Documenation = Backbone.View.extend({
  className: 'documentation',

  render: function(url) {
    this.$el.html('<iframe src=' + url + '></iframe>');
  }
});
