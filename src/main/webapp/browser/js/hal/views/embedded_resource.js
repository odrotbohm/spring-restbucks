HAL.Views.EmbeddedResource = Backbone.View.extend({
  initialize: function(opts) {
    this.vent = opts.vent;
    this.resource = opts.resource;

    this.propertiesView = new HAL.Views.Properties({});
    this.linksView = new HAL.Views.Links({
      vent: this.vent
    });

    _.bindAll(this, 'onToggleClick');
    _.bindAll(this, 'onDoxClick');
  },

  events: {
    'click a.accordion-toggle': 'onToggleClick',
    'click span.dox': 'onDoxClick'
  },

  className: 'embedded-resource accordion-group',

  onToggleClick: function(e) {
    e.preventDefault();
    this.$accordionBody.collapse('toggle');
  },
  
  onDoxClick: function(e) {
    e.preventDefault();
    this.vent.trigger('show-docs', {
      url: $(e.currentTarget).data('href')
    });
    return false;
  },

  render: function() {
    this.$el.empty();

    this.propertiesView.render(this.resource.toJSON());
    this.linksView.render(this.resource.links);

    this.$el.append(this.template({
      resource: this.resource
    }));

    var $inner = $('<div class="accordion-inner"></div>');
    $inner.append(this.propertiesView.el);
    $inner.append(this.linksView.el);

    this.$accordionBody = $('<div class="accordion-body collapse"></div>');
    this.$accordionBody.append($inner)

    this.$el.append(this.$accordionBody);
  },

  template: _.template($('#embedded-resource-template').html())
});
