package com.codeaffine.console.core.internal;

import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.TextConsolePage;
import org.eclipse.ui.part.IPageBookViewPage;

import com.codeaffine.console.core.ConsoleComponentFactory;
import com.codeaffine.console.core.ConsoleDefinition;
import com.codeaffine.console.core.ConsoleOutput;
import com.codeaffine.console.core.internal.resource.ColorDefinition;
import com.codeaffine.console.core.internal.resource.ConsoleIoProvider;

public class Console extends IOConsole {

  private final ConsoleComponentFactory consoleComponentFactory;
  private final ConsoleIoProvider consoleIoProvider;
  private final ColorDefinition colorDefinition;

  private volatile TextConsolePage consolePage;

  public Console( ConsoleDefinition definition ) {
    super( definition.getTitle(), definition.getType(), definition.getImage(), definition.getEncoding().name(), true );
    this.colorDefinition = new ColorDefinition( definition.getColorScheme() );
    this.consoleIoProvider = createConsoleIoProvider( definition, colorDefinition );
    this.consoleComponentFactory = definition.getConsoleComponentFactory();
  }

  @Override
  protected void init() {
    super.init();
  }

  @Override
  public IPageBookViewPage createPage( IConsoleView view ) {
    consolePage = ( TextConsolePage )super.createPage( view );
    return new ConsolePage( consolePage, consoleIoProvider, consoleComponentFactory );
  }

  public TextConsolePage getPage() {
    return consolePage;
  }

  @Override
  protected void dispose() {
    super.dispose();
    colorDefinition.dispose();
  }

  @Override
  public void clearConsole() {
    super.clearConsole();
    ConsoleOutput consoleOutput = Output.create( consoleIoProvider.getPromptStream(), consoleIoProvider );
    consoleComponentFactory.createConsolePrompt( consoleOutput ).writePrompt();
  }

  private ConsoleIoProvider createConsoleIoProvider( ConsoleDefinition definition, ColorDefinition colorDefinition ) {
    IoStreamProvider ioStreamProvider = new IoStreamProvider( this );
    return new ConsoleIoProvider( colorDefinition, ioStreamProvider, definition.getEncoding() );
  }
}