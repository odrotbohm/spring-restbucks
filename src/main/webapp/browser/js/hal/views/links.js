HAL.Views.Links = Backbone.View.extend({
  initialize: function(opts) {
    this.vent = opts.vent;
  },

  events: {
    'click .follow': 'followLink',
    'click .non-get': 'showNonSafeRequestDialog',
    'click .query': 'showUriQueryDialog',
    'click .dox': 'showDocs'
  },

  className: 'links',

  followLink: function(e) {
    e.preventDefault();
    var $target = $(e.currentTarget);
    var uri = $target.attr('href');
    window.location.hash = uri;
  },

  showUriQueryDialog: function(e) {
    e.preventDefault();

    var $target = $(e.currentTarget);
    var uri = $target.attr('href');

    new HAL.Views.QueryUriDialog({
      href: uri
    }).render({});
  },

  showNonSafeRequestDialog: function(e) {
    e.preventDefault();

    var d = new HAL.Views.NonSafeRequestDialog({
      href: $(e.currentTarget).attr('href'),
      vent: this.vent
    }).render({});
  },

  showDocs: function(e) {
    e.preventDefault();
    var $target = $(e.target);
    var uri = $target.attr('href') || $target.parent().attr('href');
    this.vent.trigger('show-docs', { url: uri });
  },

  template: _.template($('#links-template').html()),

  render: function(links) {
    this.$el.html(this.template({ links: links }));
  }
});
