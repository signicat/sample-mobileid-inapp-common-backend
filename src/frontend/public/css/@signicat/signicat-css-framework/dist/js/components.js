var CssComponents = function() {
    var self = this;
    
    /*Signicat Switch*/
    ko.components.register('signicat-switch', {
        viewModel: function(params) {
            var self = this;
            
            self.isSmall = params.small || false;
            self.yesValue = params.yesValue || "yes";
            self.noValue = params.noValue || "no";
            self.inputName = params.name || "not-named-input";
            self.idYes = self.inputName + "-" + self.yesValue;
            self.idNo = self.inputName + "-" + self.noValue;
            
            //Needs to be observable
            self.checkedValue = params.checkedValue || ko.observable(self.yesValue);
        },
        template:
        '<div class="signicat-switch" data-bind="css: { small: isSmall }">'
             + '<input data-bind="attr: { name: inputName, id: idYes, value: yesValue}, checked: checkedValue" type="radio" class="radio-yes">'
             + '<input data-bind="attr: { name: inputName, id: idNo, value: noValue}, checked: checkedValue" type="radio" class="radio-no">'
             + '<div class="radio-switch-wrapper">'
                + '<div class="radio-switch">'
                    + '<label class="radio-option left-option" data-bind="attr: { for: idNo}"></label>'
                    + '<div class="switch"></div>'
                    + '<label class="radio-option right-option" data-bind="attr: { for: idYes}"></label>'
                + '</div>'
            + '</div>'
        + '</div>'
    });
    
    /*Signicat Checkbox*/
    ko.components.register('signicat-checkbox', {
        viewModel: function(params) {
            var self = this;
            
            self.label = params.label || false;
            self.name = params.name;
            self.value = params.value;
            if (params.required) {
                self.required = "required";
            } else {
                self.required = "off";
            }
            
            //Needs to be observable
            self.checked = params.checked || false;
        },
        template:
            '<label class="signicat-checkbox">'
                + '<input type="checkbox" data-bind="attr:{ name: name, value: value, required: required}, checked: checked">'
                + '<div class="checkbox">'
                    + '<div class="checked-icon"></div>'
                + '</div>'
                + '<span class="checkbox-text" data-bind="text: label"></span>'
            + '</label>'
    });
    
    ko.components.register('signicat-header', {
        viewModel: function(params) {
            var self = this;
            
            self.menuOpen = ko.observable(false);
            
            if (params.sticky === false) {
                self.sticky = false;
            } else {
                self.sticky = true;
                document.body.classList.add("menu-padding");
            }
            
            self.logo = params.logo || "logo/signicat-logo.svg";
            self.logoLink = params.logoLink || "";
            
            self.toggleMenu = function() {
                self.menuOpen(!self.menuOpen());
            }
            
            if (self.sticky) {
                //Add menu padding if menu is sticky
                document.body.classList.add("menu-padding");
            }
            
        },
        template:
        '<header class="signicat-header" data-bind="css:{ sticky: sticky, \'menu-open\': menuOpen}">'
            + '<div class="header-wrapper">'
                + '<div class="menu-toggler" data-bind="click: toggleMenu">'
                    + '<i class="material-icons open-menu-icon" data-bind="visible: !menuOpen()">&#xE5D2;</i>'
                    + '<i class="material-icons close-menu-icon" data-bind="visible: menuOpen">&#xE5CD;</i>'
                    + '<span data-l10n-id="signicat-header-menu">Menu</span>'
                + '</div>'
                + '<a class="logo" href="">'
                    + '<img src="" data-bind="attr: {src: logo}">'
                + '</a>'
                + '<div class="close-menu-wrapper" data-bind="visible: menuOpen, click: toggleMenu"></div>'
                + '<nav data-bind="css:{\'slide-in\': menuOpen}">'
                    + '<!-- ko template: { nodes: $componentTemplateNodes } --><!-- /ko -->'
                + '</nav>'
            + '</div>'
        + '</header>'
    });    
    
    ko.components.register('signicat-notification', {
        viewModel: function(params) {
            var self = this;
            
            self.notificationVisible = params.notificationVisible || ko.observable(true);
            self.hideNotification = function() {
                self.notificationVisible(false);
            };
        },
        template:
        '<div class="notification-wrapper" data-bind="visible: notificationVisible">'
            + '<div class="notification-close-wrapper" data-bind="click: hideNotification"></div>'
            + '<div class="notification-message">'
                 + '<!-- ko template: { nodes: $componentTemplateNodes } --><!-- /ko -->'
            + '</div>'
        + '</div>'
    });
    
    ko.components.register('signicat-spinner', {
        viewModel: function(params) {
            var self = this;
            
            self.fixed = params.fixed || "";
            self.inline = params.inline || false;
            self.small = params.small || false;
        },
        template:
        '<div class="spinner-wrapper" data-bind="css: { fixed: fixed, \'inline-wrapper\': inline }">'
            + '<div class="spinner-inner-child">'
                + '<div class="circlespinner" data-bind="css: { inline: inline, small: small }">'
                    + '<div class="circle-border"></div>'
                + '</div>'
            + '</div>'
        + '</div>'
    });
    
    ko.components.register('signicat-popup', {
        viewModel: function(params) {
            var self = this;
            
            //Required
            self.popUpVisible = params.popUpVisible;
            
            self.showCloseText = params.showCloseText || false;
            
            self.fixed = params.fixed || false;
            
            self.hidePopUp = function() {
                self.popUpVisible(false);
            };
        },
        template:
        '<div class="signicat-pop-up" data-bind="visible: popUpVisible, css:{ fixed: fixed}">'
            + '<div class="signicat-pop-up-inner">'
                + '<div class="pop-up-close" data-bind="click: hidePopUp">'
                    + '<i class="material-icons" alt="close">close</i>'
                    + '<span data-bind="visible: showCloseText">Close</span>'
                + '</div>'
                + '<!-- ko template: { nodes: $componentTemplateNodes } --><!-- /ko -->'
            + '</div>'
        + '</div>'
    });
};