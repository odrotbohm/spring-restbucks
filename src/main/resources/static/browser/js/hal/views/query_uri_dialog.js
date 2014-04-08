HAL.Views.QueryUriDialog = Backbone.View.extend({
  initialize: function(opts) {
    this.href = opts.href;
    this.uriTemplate = uritemplate(this.href);
    _.bindAll(this, 'submitQuery');
    _.bindAll(this, 'renderPreview');
  },

  className: 'modal fade',

  events: {
    'submit form': 'submitQuery',
    'keyup textarea': 'renderPreview',
    'change textarea': 'renderPreview'
  },

  submitQuery: function(e) {
    e.preventDefault();
    var input;
    try {
      input = JSON.parse(this.$('textarea').val());
    } catch(err) {
      input = {};
    }
    this.$el.modal('hide');
    window.location.hash = this.uriTemplate.expand(input);
  },

  renderPreview: function(e) {
    var input, result;
    try {
      input = JSON.parse($(e.target).val());
      result = this.uriTemplate.expand(input);
    } catch (err) {
      result = 'Invalid json input';
    }
    this.$('.preview').html(result);
  },

  extractExpressionNames: function (template) {
    var names = [];
    for (var i=0; i<template.set.length; i++) {
      if (template.set[i].vars) {
        for (var j=0; j<template.set[i].vars.length; j++) {
          names.push(template.set[i].vars[j].name);
        }
      }
    }
    return names;
  },

  createDefaultInput: function (expressionNames) {
    var defaultInput = {};
    for (var i=0; i<expressionNames.length; i++) {
      defaultInput[expressionNames[i]] = '';
    }
    return JSON.stringify(defaultInput, null, HAL.jsonIndent);
  },

  render: function(opts) {
    var input = this.createDefaultInput(this.extractExpressionNames(this.uriTemplate));
    this.$el.html(this.template({ href: this.href, input: input }));
    this.$('textarea').trigger('keyup');
    this.$el.modal(opts);
    return this;
  },

  template: _.template($('#query-uri-template').html())
});
