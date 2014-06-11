package com.codeaffine.gonsole.acceptance;

import static com.codeaffine.gonsole.test.helper.CompositeRepositoryProviderHelper.createWithSingleChildProvider;
import static com.codeaffine.test.util.swt.SWTEventHelper.trigger;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsolePage;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.codeaffine.gonsole.internal.GitConsole;
import com.codeaffine.gonsole.pdetest.ViewHelper;
import com.codeaffine.test.util.swt.DisplayHelper;

public class GitConsoleRule implements MethodRule {

  private static final String CONSOLE_VIEW_ID = "org.eclipse.ui.console.ConsoleView";
  private static final String INTRO_VIEW_ID = "org.eclipse.ui.internal.introview";

  private final ViewHelper viewHelper;

  private TextConsolePage textConsolePage;

  public GitConsoleRule() {
    viewHelper = new ViewHelper();
  }

  public static TextConsolePageAssert assertThat( GitConsoleRule actual ) {
    return TextConsolePageAssert.assertThat( actual.textConsolePage );
  }

  @Override
  public Statement apply( final Statement base, FrameworkMethod method, Object target ) {
    return new GitConsoleRuleStatement( this, base );
  }

  public GitConsoleRule typeText( String text ) {
    StyledText control = ( StyledText )textConsolePage.getControl();
    for( int i = 0; i < text.length(); i++ ) {
      triggerEvent( control, SWT.KeyDown, text.charAt( i ) );
      triggerEvent( control, SWT.KeyUp, text.charAt( i ) );
    }
    return this;
  }

  public GitConsoleRule positionCaret( int caretOffset ) {
    StyledText control = ( StyledText )textConsolePage.getControl();
    control.setCaretOffset( caretOffset );
    return this;
  }

  public void enterCommandLine( String commandLine ) {
    checkState( textConsolePage != null, "GitConsole has not been opened yet." );

    StyledText control = ( StyledText )textConsolePage.getControl();
    String lineDelimiter = control.getLineDelimiter();
    control.append( commandLine + lineDelimiter );
  }

  public void open( File ... repositoryLocations ) {
    viewHelper.hideView( INTRO_VIEW_ID );
    GitConsole console = registerNewGitConsole( repositoryLocations );
    showInView( console );
    textConsolePage = console.getPage();
    new TextViewerChangeObserver( textConsolePage.getViewer() ).waitForChange();
  }

  void cleanup() {
    removeGitConsoles();
    textConsolePage = null;
    viewHelper.hideView( CONSOLE_VIEW_ID );
  }

  private static void triggerEvent( StyledText control, int eventType, char character ) {
    trigger( eventType ).withKeyCode( character ).withCharacter( character ).on( control );
  }

  private static GitConsole registerNewGitConsole( File[] repositoryLocations ) {
    GitConsole result = new GitConsole( createWithSingleChildProvider( repositoryLocations ) );
    ConsolePlugin.getDefault().getConsoleManager().addConsoles( new IConsole[] { result } );
    return result;
  }

  private IConsoleView showInView( IConsole console ) {
    IConsoleView result = ( IConsoleView )viewHelper.showView( CONSOLE_VIEW_ID );
    result.display( console );
    new DisplayHelper().flushPendingEvents();
    return result;
  }

  private static void removeGitConsoles() {
    IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
    for( IConsole console : consoleManager.getConsoles() ) {
      if( console instanceof GitConsole ) {
        consoleManager.removeConsoles( new IConsole[] { console } );
      }
    }
  }
}