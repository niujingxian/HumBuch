package de.dhbw.humbuch.view;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.davherrmann.mvvm.BasicState;
import de.davherrmann.mvvm.State;
import de.davherrmann.mvvm.StateChangeListener;
import de.davherrmann.mvvm.ViewModelComposer;
import de.davherrmann.mvvm.annotations.BindState;
import de.dhbw.humbuch.event.ConfirmEvent;
import de.dhbw.humbuch.event.MessageEvent;
import de.dhbw.humbuch.model.entity.Category;
import de.dhbw.humbuch.model.entity.Profile;
import de.dhbw.humbuch.model.entity.SchoolYear.Term;
import de.dhbw.humbuch.model.entity.Subject;
import de.dhbw.humbuch.model.entity.TeachingMaterial;
import de.dhbw.humbuch.viewmodel.BookManagementViewModel;
import de.dhbw.humbuch.viewmodel.BookManagementViewModel.Categories;
import de.dhbw.humbuch.viewmodel.BookManagementViewModel.TeachingMaterials;

/**
 * 
 * @author Martin Wentzel
 * 
 */
public class BookManagementView extends VerticalLayout implements View,
		ViewInformation {
	private static final long serialVersionUID = -5063268947544706757L;

	private static final String TITLE = "Lehrmittelverwaltung";

	private static final String TABLE_CATEGORY = "category";
	private static final String TABLE_NAME = "name";
	private static final String TABLE_PROFILE = "profile";
	private static final String TABLE_FROMGRADE = "fromGrade";
	private static final String TABLE_FROMTERM = "fromTerm";
	private static final String TABLE_TOGRADE = "toGrade";
	private static final String TABLE_TOTERM = "toTerm";
	private static final String TABLE_PRODUCER = "producer";
	private static final String TABLE_IDENTNR = "identifyingNumber";
	private static final String TABLE_COMMENT = "comment";
	private static final String TABLE_VALIDFROM = "validFrom";
	private static final String TABLE_VALIDUNTIL = "validUntil";

	private EventBus eventBus;
	private BookManagementViewModel bookManagementViewModel;

	/**
	 * Layout components
	 */
	private HorizontalLayout head;
	private TextField filter;

	private Button btnEdit;
	private Button btnNew;
	private Button btnDelete;

	private Table materialsTable;
	private BeanItemContainer<TeachingMaterial> tableData;
	private BeanFieldGroup<TeachingMaterial> binder = new BeanFieldGroup<TeachingMaterial>(
			TeachingMaterial.class);
	@BindState(TeachingMaterials.class)
	public final State<Collection<TeachingMaterial>> teachingMaterials = new BasicState<>(
			Collection.class);
	@BindState(Categories.class)
	public final State<Collection<Category>> categories = new BasicState<>(
			Collection.class);

	/**
	 * All popup-window components and the corresponding binded states. The
	 * popup-window for adding a new teaching material and editing a teaching
	 * material is the same. Only the caption will be set differently
	 */
	private Window windowEditTeachingMaterial;
	private FormLayout windowContent;
	private HorizontalLayout windowButtons;
	private TextField txtTmName = new TextField("Titel");
	private TextField txtIdentNr = new TextField("Nummer/ISBN");
	private TextField txtProducer = new TextField("Hersteller/Verlag");
	private TextField txtFromGrade = new TextField();
	private TextField txtToGrade = new TextField();
	private ComboBox cbProfiles = new ComboBox("Profil");
	private ComboBox cbFromTerm = new ComboBox();
	private ComboBox cbToTerm = new ComboBox();
	private ComboBox cbCategory = new ComboBox("Kategorie");
	private DateField dfValidFrom = new DateField("Gültig von");
	private DateField dfValidUntil = new DateField("Gültig bis");
	private TextArea textAreaComment = new TextArea("Kommentar");
	private Button btnWindowSave = new Button("Speichern");
	private Button btnWindowCancel = new Button("Abbrechen");

	@Inject
	public BookManagementView(ViewModelComposer viewModelComposer,
			BookManagementViewModel bookManagementViewModel, EventBus eventBus) {
		this.bookManagementViewModel = bookManagementViewModel;
		this.eventBus = eventBus;
		init();
		buildLayout();
		bindViewModel(viewModelComposer, bookManagementViewModel);
	}

	/**
	 * Initializes the components and sets attributes.
	 * 
	 */
	@SuppressWarnings("serial")
	private void init() {

		head = new HorizontalLayout();
		head.setWidth("100%");
		head.setSpacing(true);

		// Filter
		filter = new TextField();
		filter.setImmediate(true);
		filter.setInputPrompt("Lehrmittel suchen...");
		filter.setWidth("50%");
		filter.setTextChangeEventMode(TextChangeEventMode.EAGER);

		head.addComponent(filter);
		head.setExpandRatio(filter, 1);
		head.setComponentAlignment(filter, Alignment.MIDDLE_LEFT);

		// Buttons
		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);

		// Add
		btnNew = new Button("Hinzufügen");
		buttons.addComponent(btnNew);

		// Delte
		btnDelete = new Button("Löschen");
		btnDelete.setEnabled(false);
		buttons.addComponent(btnDelete);

		// Edit
		btnEdit = new Button("Bearbeiten");
		btnEdit.setEnabled(false);
		btnEdit.addStyleName("default");
		btnEdit.setClickShortcut(KeyCode.ENTER);
		buttons.addComponent(btnEdit);

		head.addComponent(buttons);
		head.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);

		// Instantiate table
		materialsTable = new Table();
		materialsTable.setSelectable(true);
		materialsTable.setImmediate(true);
		materialsTable.setSizeFull();
		materialsTable.setColumnCollapsingAllowed(true);

		tableData = new BeanItemContainer<TeachingMaterial>(
				TeachingMaterial.class);
		materialsTable.setContainerDataSource(tableData);

		materialsTable.setVisibleColumns(new Object[] { TABLE_NAME,
				TABLE_PROFILE, TABLE_FROMGRADE, TABLE_TOGRADE, TABLE_PRODUCER,
				TABLE_IDENTNR, TABLE_CATEGORY });
		materialsTable.setColumnHeader(TABLE_CATEGORY, "Kategorie");
		materialsTable.setColumnHeader(TABLE_NAME, "Titel");
		materialsTable.setColumnHeader(TABLE_PROFILE, "Profil");
		materialsTable.setColumnHeader(TABLE_FROMGRADE, "Von Klasse");
		materialsTable.setColumnHeader(TABLE_TOGRADE, "Bis Klasse");
		materialsTable.setColumnHeader(TABLE_PRODUCER, "Hersteller/Verlag");
		materialsTable.setColumnHeader(TABLE_IDENTNR, "Nummer/ISBN");
		materialsTable.setColumnHeader(TABLE_COMMENT, "Kommentar");
		materialsTable.setColumnHeader(TABLE_VALIDFROM, "Gültig von");
		materialsTable.setColumnHeader(TABLE_VALIDUNTIL, "Gültig bis");
		materialsTable.addGeneratedColumn(TABLE_PROFILE, new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, Object itemId,
					Object columnId) {
				TeachingMaterial item = (TeachingMaterial) itemId;
				String profile = "";
				for (Entry<String, Set<Subject>> entry : Profile
						.getProfileMap().entrySet()) {
					if (item.getProfile().equals(entry.getValue())) {
						profile = entry.getKey().toString();
						break;
					}
				}
				return profile;
			}
		});

		binder.setBuffered(true);

		this.createEditWindow();
		this.addListener();
	}

	/**
	 * Creates the window for editing and creating teaching materials
	 * 
	 * @return The created Window
	 */
	@SuppressWarnings("serial")
	public void createEditWindow() {
		// Create Window and set parameters
		windowEditTeachingMaterial = new Window();
		windowEditTeachingMaterial.center();
		windowEditTeachingMaterial.setModal(true);
		windowEditTeachingMaterial.setResizable(false);

		windowContent = new FormLayout();
		windowContent.setMargin(true);
		windowButtons = new HorizontalLayout();
		windowButtons.setSpacing(true);

		btnWindowSave.addStyleName("default");

		// Fill Comboboxes
		cbFromTerm.addItem(Term.FIRST);
		cbFromTerm.addItem(Term.SECOND);

		cbToTerm.addItem(Term.FIRST);
		cbToTerm.addItem(Term.SECOND);

		Map<String, Set<Subject>> profiles = Profile.getProfileMap();
		for (Map.Entry<String, Set<Subject>> profile : profiles.entrySet()) {
			cbProfiles.addItem(profile.getValue());
			cbProfiles.setItemCaption(profile.getValue(), profile.getKey());
		}

		// Set Form options
		cbCategory.setNullSelectionAllowed(false);
		cbProfiles.setNullSelectionAllowed(false);
		cbFromTerm.setNullSelectionAllowed(false);
		cbToTerm.setNullSelectionAllowed(false);
		txtIdentNr.setRequired(true);
		txtTmName.setRequired(true);
		cbCategory.setRequired(true);
		cbProfiles.setRequired(true);
		dfValidFrom.setRequired(true);
		dfValidUntil.setDescription("Leer für unbestimmtes Gültigkeitsdatum");

		// Input prompts
		txtIdentNr.setInputPrompt("ISBN");
		txtTmName.setInputPrompt("Titel oder Name");
		txtProducer.setInputPrompt("z.B. Klett");
		txtFromGrade.setInputPrompt("z.B. 5");
		txtToGrade.setInputPrompt("z.B. 7");
		textAreaComment.setInputPrompt("Zusätzliche Informationen");

		// NullRepresentation
		txtIdentNr.setNullRepresentation("");
		txtTmName.setNullRepresentation("");
		txtProducer.setNullRepresentation("");
		txtFromGrade.setNullRepresentation("");
		txtToGrade.setNullRepresentation("");
		textAreaComment.setNullRepresentation("");

		// Bind to FieldGroup
		binder.bind(txtTmName, TABLE_NAME);
		binder.bind(txtIdentNr, TABLE_IDENTNR);
		binder.bind(cbCategory, TABLE_CATEGORY);
		binder.bind(txtProducer, TABLE_PRODUCER);
		binder.bind(cbProfiles, TABLE_PROFILE);
		binder.bind(txtFromGrade, TABLE_FROMGRADE);
		binder.bind(cbFromTerm, TABLE_FROMTERM);
		binder.bind(txtToGrade, TABLE_TOGRADE);
		binder.bind(cbToTerm, TABLE_TOTERM);
		binder.bind(dfValidFrom, TABLE_VALIDFROM);
		binder.bind(dfValidUntil, TABLE_VALIDUNTIL);
		binder.bind(textAreaComment, TABLE_COMMENT);

		// Add all components
		windowContent.addComponent(txtTmName);
		windowContent.addComponent(txtIdentNr);
		windowContent.addComponent(cbCategory);
		windowContent.addComponent(txtProducer);
		windowContent.addComponent(cbProfiles);
		windowContent.addComponent(new HorizontalLayout() {
			{
				addStyleName("required");
				setSpacing(true);
				setCaption("Von Klassenstufe");
				txtFromGrade.setWidth("50px");
				cbFromTerm.setWidth(null);
				addComponent(txtFromGrade);
				addComponent(cbFromTerm);
			}
		});
		windowContent.addComponent(new HorizontalLayout() {
			{
				addStyleName("required");
				setSpacing(true);
				setCaption("Bis Klassenstufe");
				txtToGrade.setWidth("50px");
				addComponent(txtToGrade);
				addComponent(cbToTerm);
			}
		});
		windowContent.addComponent(dfValidFrom);
		windowContent.addComponent(dfValidUntil);
		windowContent.addComponent(textAreaComment);

		windowButtons.addComponent(btnWindowCancel);
		windowButtons.addComponent(btnWindowSave);
		windowContent.addComponent(windowButtons);
		windowEditTeachingMaterial.setContent(windowContent);

		windowEditTeachingMaterial.setCaption("Lehrmittel bearbeiten");
		windowEditTeachingMaterial.setCloseShortcut(KeyCode.ESCAPE, null);
	}

	/**
	 * Adds all listener to their corresponding components.
	 * 
	 */
	@SuppressWarnings("serial")
	private void addListener() {

		/**
		 * Listens for changes in the Collection teachingMaterials and adds them
		 * to the container
		 */
		teachingMaterials.addStateChangeListener(new StateChangeListener() {
			@Override
			public void stateChange(Object value) {
				tableData.removeAllItems();
				for (TeachingMaterial material : teachingMaterials.get()) {
					tableData.addItem(material);
				}
			}
		});

		/**
		 * Enables/disables the edit and delete buttons
		 */
		materialsTable.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				TeachingMaterial item = (TeachingMaterial) materialsTable
						.getValue();
				btnEdit.setEnabled(item != null);
				btnDelete.setEnabled(item != null);
			}
		});

		/**
		 * Opens the popup-window for editing a book and inserts the data from
		 * the teachingMaterialInfo-State.
		 */
		btnEdit.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				TeachingMaterial item = (TeachingMaterial) materialsTable
						.getValue();
				binder.setItemDataSource(item);
				UI.getCurrent().addWindow(windowEditTeachingMaterial);
				txtTmName.focus();
			}
		});

		/**
		 * Opens a new popup-window with an empty new teaching material
		 */
		btnNew.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				TeachingMaterial item = new TeachingMaterial.Builder(null,
						null, null, new Date()).build();
				binder.setItemDataSource(item);
				UI.getCurrent().addWindow(windowEditTeachingMaterial);
				txtTmName.focus();
			}
		});

		/**
		 * Closes the popup window
		 * 
		 */
		btnWindowCancel.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				windowEditTeachingMaterial.close();
				clearWindowFields();
			}
		});

		/**
		 * Saves the teachingMaterial and closes the popup-window
		 * 
		 */
		btnWindowSave.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				if (FormFieldsValid()) {
					try {
						binder.commit();
						bookManagementViewModel.doUpdateTeachingMaterial(binder
								.getItemDataSource().getBean());
						windowEditTeachingMaterial.close();
						eventBus.post(new MessageEvent(
								"Lehrmittel gespeichert."));
					} catch (CommitException e) {
						eventBus.post(new MessageEvent(e.getLocalizedMessage()));
					}

				}

			}
		});

		/**
		 * Show a confirm popup and delete the teaching material on confirmation
		 */
		btnDelete.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				final TeachingMaterial item = (TeachingMaterial) materialsTable
						.getValue();
				if (item != null) {
					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							bookManagementViewModel
									.doDeleteTeachingMaterial(item);
							materialsTable.select(null);
						}
					};
					eventBus.post(new ConfirmEvent.Builder(
							"Wollen Sie das Lehrmittel wirklich löschen?")
							.caption("Löschen").confirmRunnable(runnable)
							.build());
				}

			}
		});

		/**
		 * Provides the live search of the teaching material table by adding a
		 * filter after every keypress in the search field. Currently the
		 * publisher and title search are supported.
		 */
		filter.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = -1684545652234105334L;

			@Override
			public void textChange(TextChangeEvent event) {
				Filter filter = new Or(new SimpleStringFilter(TABLE_NAME, event
						.getText(), true, false), new SimpleStringFilter(
						TABLE_PRODUCER, event.getText(), true, false));
				tableData.removeAllContainerFilters();
				tableData.addContainerFilter(filter);
			}
		});

		/**
		 * Allows to dismiss the filter by hitting ESCAPE
		 */
		filter.addShortcutListener(new ShortcutListener("Clear",
				KeyCode.ESCAPE, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				filter.setValue("");
				tableData.removeAllContainerFilters();
			}
		});

		/**
		 * Fills the category combobox
		 */
		categories.addStateChangeListener(new StateChangeListener() {
			@Override
			public void stateChange(Object arg0) {
				for (Category cat : categories.get()) {
					cbCategory.addItem(cat);
					cbCategory.setItemCaption(cat, cat.getName());
				}
			}
		});

	}

	/**
	 * Validates the fields in the edit teaching materials window.
	 * 
	 * @return Whether or not the fields in the editor are valid
	 */
	private boolean FormFieldsValid() {
		// Validate if a field is empty
		if (txtTmName.getValue() == null || txtIdentNr.getValue() == null
				|| cbCategory.getValue() == null
				|| dfValidFrom.getValue() == null
				|| txtFromGrade.getValue() == null
				|| txtToGrade.getValue() == null
				|| cbProfiles.getValue() == null) {

			eventBus.post(new MessageEvent(
					"Bitte füllen Sie alle Pflicht-Felder aus."));
			return false;

		} else {

			// No field is empty, validate now for right values and lengths
			if (txtTmName.getValue().length() < 2) {
				eventBus.post(new MessageEvent(
						"Der Titel muss mindestens 2 Zeichen enthalten."));
				return false;
			}
			if (txtFromGrade.getValue().length() > 2
					|| txtToGrade.getValue().length() > 2) {
				eventBus.post(new MessageEvent(
						"Die Klassenstufen dürfen höchstens 2 Zeichen enthalten"));
				return false;
			}
			try {
				Integer.parseInt(txtToGrade.getValue());
				Integer.parseInt(txtFromGrade.getValue());
			} catch (NumberFormatException e) {
				eventBus.post(new MessageEvent(
						"Die Klassenstufen dürfen nur Zahlen enthalten"));
				return false;
			}
			try {
				dfValidFrom.validate();
				dfValidUntil.validate();
			} catch (InvalidValueException e) {
				eventBus.post(new MessageEvent(
						"Mindestens ein Datumsfeld ist nicht korrekt."));
				return false;
			}
			return true;

		}
	}

	/**
	 * Builds the layout by adding all components in their specific order.
	 */
	private void buildLayout() {
		setMargin(true);
		setSpacing(true);
		setSizeFull();

		addComponent(head);
		addComponent(materialsTable);
		setExpandRatio(materialsTable, 1);
	}

	/**
	 * Empties the fields of the popup-window to prevent that TextFields already
	 * have content form previous edits.
	 */
	private void clearWindowFields() {
		txtTmName.setValue("");
		txtIdentNr.setValue("");
		txtProducer.setValue("");
		txtFromGrade.setValue("");
		txtToGrade.setValue("");
		textAreaComment.setValue("");
		txtProducer.setValue("");
		cbFromTerm.setValue(Term.FIRST);
		cbToTerm.setValue(Term.SECOND);
	}

	@Override
	public void enter(ViewChangeEvent event) {
	}

	private void bindViewModel(ViewModelComposer viewModelComposer,
			Object... viewModels) {
		try {
			viewModelComposer.bind(this, viewModels);
		} catch (IllegalAccessException | NoSuchElementException
				| UnsupportedOperationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getTitle() {
		return TITLE;
	}
}
