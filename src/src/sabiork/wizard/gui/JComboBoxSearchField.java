package sabiork.wizard.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.xml.stream.XMLStreamException;
import sabiork.SABIORK;
import sabiork.util.WebServiceConnectException;
import sabiork.util.WebServiceResponseException;

/**
 * A class that provides a search field with suggestion support according to a
 * given SABIO-RK query field.
 * 
 * @author Matthias Rall
 * 
 */
@SuppressWarnings("serial")
public class JComboBoxSearchField extends JComboBox implements KeyListener {

	private JTextField textFieldSearch;
	private SABIORK.QueryField suggestionQueryField;
	private SuggestionSearch suggestionSearch;

	public JComboBoxSearchField() {
		setEditable(true);
		this.textFieldSearch = (JTextField) getEditor().getEditorComponent();
		this.textFieldSearch.addKeyListener(this);
		this.suggestionQueryField = null;
		this.suggestionSearch = null;
	}

	/**
	 * Shows all suggestions.
	 * 
	 * @param suggestions
	 */
	private void showSuggestions(List<String> suggestions) {
		if (!suggestions.isEmpty()) {
			String currentText = textFieldSearch.getText();
			setModel(new DefaultComboBoxModel(suggestions.toArray()));
			if (!suggestions.contains(currentText.trim())) {
				showPopup();
			}
			textFieldSearch.setText(currentText);
		}
	}

	/**
	 * Returns the text of the search field.
	 * 
	 * @return
	 */
	public String getText() {
		return textFieldSearch.getText();
	}

	/**
	 * Returns the selected SABIO-RK query field for suggestions.
	 * 
	 * @return
	 */
	public SABIORK.QueryField getSuggestionQueryField() {
		return suggestionQueryField;
	}

	/**
	 * Sets the text of the search field.
	 * 
	 * @return
	 */
	public void setText(String text) {
		textFieldSearch.setText(text);
	}

	/**
	 * Sets the selected SABIO-RK query field for suggestions.
	 * 
	 * @return
	 */
	public void setSuggestionQueryField(SABIORK.QueryField suggestionQueryField) {
		this.suggestionQueryField = suggestionQueryField;
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		if (e.getSource().equals(textFieldSearch)) {
			if (e.getKeyCode() != KeyEvent.VK_UP
					&& e.getKeyCode() != KeyEvent.VK_DOWN
					&& e.getKeyCode() != KeyEvent.VK_LEFT
					&& e.getKeyCode() != KeyEvent.VK_RIGHT
					&& e.getKeyCode() != KeyEvent.VK_ESCAPE) {
				if (suggestionSearch != null && suggestionSearch.isStarted()) {
					suggestionSearch.cancel();
				}
				hidePopup();
				String currentTextTrimmed = textFieldSearch.getText().trim();
				if (suggestionQueryField != null
						&& !currentTextTrimmed.isEmpty()) {
					suggestionSearch = new SuggestionSearch(
							suggestionQueryField, currentTextTrimmed);
					suggestionSearch.execute();
				}
			}
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				textFieldSearch.setText((String) getSelectedItem());
			}
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	/**
	 * A class to perform the search for suggestions.
	 * 
	 * @author Matthias Rall
	 * 
	 */
	private class SuggestionSearch extends SwingWorker<List<String>, Void> {

		private SABIORK.QueryField queryField;
		private String partialString;

		public SuggestionSearch(SABIORK.QueryField queryField,
				String partialString) {
			this.queryField = queryField;
			this.partialString = partialString;
		}

		/**
		 * Checks if this search is already in progress.
		 * 
		 * @return <code>true</code> if this search is already in progress,
		 *         <code>false</code> otherwise
		 */
		public boolean isStarted() {
			return (getState() == StateValue.STARTED);
		}

		/**
		 * Cancels this search.
		 */
		public void cancel() {
			cancel(true);
		}

		/**
		 * Performs the search process.
		 */
		protected List<String> doInBackground() {
			List<String> suggestions = new ArrayList<String>();
			try {
				suggestions = SABIORK.getSuggestions(queryField, partialString);
			} catch (WebServiceConnectException e) {
				JDialogWizard.showErrorDialog(e);
				e.printStackTrace();
			} catch (WebServiceResponseException e) {
				JDialogWizard.showErrorDialog(e);
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
			return suggestions;
		}

		/**
		 * Displays the results.
		 */
		protected void done() {
			try {
				showSuggestions(get());
			} catch (CancellationException e) {
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

	}

}
