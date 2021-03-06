package de.dhbw.humbuch.view.components;

import java.util.Iterator;

import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;

import de.dhbw.humbuch.view.MainUI;

public class Sidebar extends VerticalLayout {
	private static final long serialVersionUID = -2263554457706891669L;

	/**
	 * Each navigation button has to be added to the array "navigationButtons".
	 * First value of the array is the human readable caption of the button, the
	 * second one the name of the specific view to load.
	 */
	// TODO refactor when a good navigation method is found
	private static final String[][] navigationButtons = new String[][] {
			{ "Ausleihe", MainUI.LENDING_VIEW },
			{ "Rückgabe", MainUI.RETURN_VIEW },
			{ "Lehrmittel", MainUI.BOOK_MANAGEMENT_VIEW },
			{ "Mahnungen", MainUI.DUNNING_VIEW },
			{ "Schüler", MainUI.STUDENT_INFORMATION_VIEW } };

	private VerticalLayout menu;
	private NativeButton btnLogout;
	private NativeButton btnSettings;
	
	public Sidebar() {
		init();
	}
	
	@SuppressWarnings("serial")
	private void init() {
        CssLayout branding = new CssLayout() {
        	{
				addStyleName("branding");
		        Label logo = new Label(
		                "<span>HumBuch</span> Buchverwaltung",
		                ContentMode.HTML);
		        logo.setSizeUndefined();
		        addComponent(logo);
        	}
        };
        addComponent(branding);
        
		/*
		 * TODO insert the correct constants for the corresponding screens
		 */
		menu = new VerticalLayout();
		menu.addStyleName("menu");

		for (final String[] view : navigationButtons) {
			Button b = new NativeButton(view[0]);

			b.addStyleName("icon-" + view[1]);
			b.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					clearMenuBar();
					event.getButton().addStyleName("selected");

					// Navigate to the specific View
					if (!getUI().getNavigator().getState().equals(view[1])) {
						getUI().getNavigator().navigateTo(view[1]);
					}

				}
			});
			String f = Page.getCurrent().getUriFragment();
			if (f.substring(1).equals(view[1])) {
				b.addStyleName("selected");
			}

			menu.addComponent(b);
		}
		addComponent(menu);
		setExpandRatio(menu, 1);

		VerticalLayout userButtons = new VerticalLayout();
		userButtons.setSizeUndefined();
		userButtons.addStyleName("user");

		btnSettings = new NativeButton();
		btnSettings.addStyleName("icon-settings");
		btnSettings.setDescription("Einstellungen");
		userButtons.addComponent(btnSettings);
		btnSettings.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				clearMenuBar();
				btnSettings.addStyleName("selected");
				if (!getUI().getNavigator().getState()
						.equals(MainUI.SETTINGS_VIEW)) {
					getUI().getNavigator().navigateTo(MainUI.SETTINGS_VIEW);
				}
			}

		});

		btnLogout = new NativeButton();
		btnLogout.addStyleName("icon-logout");
		btnLogout.setDescription("Logout");
		userButtons.addComponent(btnLogout);

		addComponent(userButtons);

		setHeight("100%");
		addStyleName("sidebar");
	}

	/**
	 * Removes the style 'selected' from the menu buttons
	 */
	private void clearMenuBar() {
		for (Iterator<Component> it = menu.iterator(); it.hasNext();) {
			Component next = it.next();
			if (next instanceof NativeButton) {
				next.removeStyleName("selected");
			}
		}
		btnSettings.removeStyleName("selected");
	}
	
	public Button getLogoutButton() {
		return btnLogout;
	}
}
