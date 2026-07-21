# Launchly

Protótipo funcional de launcher Android feito com Kotlin e Jetpack Compose. O app usa diretamente as APIs de launcher do sistema, sem banco de dados, DI ou camadas arquiteturais desnecessárias.

## O que está implementado

- solicitação do papel de launcher padrão com `RoleManager.ROLE_HOME`;
- listagem de apps e perfis acessíveis com `LauncherApps.getActivityList`;
- abertura de apps com `LauncherApps.startMainActivity`;
- página central totalmente transparente, mostrando o wallpaper do sistema — inclusive wallpapers animados — por `FLAG_SHOW_WALLPAPER`;
- navegação horizontal: favoritos à esquerda, wallpaper no centro e todos os apps à direita;
- gesto para cima na página central abre uma busca de tela cheia;
- orientação gestual discreta na primeira execução; ela desaparece no primeiro gesto e a tela central volta a exibir somente o wallpaper;
- visual minimalista inspirado em launchers tipográficos: o wallpaper permanece visível em todas as páginas, sob uma camada de contraste discreta;
- Material 3 como base de acessibilidade, cores dinâmicas, gestos e motion, sem decoração excessiva;
- favoritos persistidos em `SharedPreferences`;
- long press com shortcuts dinâmicos, estáticos, fixados e em cache;
- busca pelos nomes curto e longo dos shortcuts — por exemplo, “Nova guia anônima” quando o Chrome o publica;
- execução com `LauncherApps.startShortcut`;
- atualização automática por `LauncherApps.Callback`, inclusive `onShortcutsChanged`;
- informações do app com `startAppDetailsActivity` e desinstalação pela tela de confirmação do Android;
- suporte a perfil de trabalho e, quando disponibilizado pelo sistema, perfil privado.

Shortcuts de outros apps são protegidos pelo Android. Eles aparecem somente depois que este app é o launcher padrão e `LauncherApps.hasShortcutHostPermission()` retorna `true`.

## Estrutura

- `MainActivity.kt`: estado enxuto, janela sobre o wallpaper e solicitação de `ROLE_HOME`.
- `LauncherUi.kt`: pager, páginas de favoritos/apps, busca e folha de ações.
- `LauncherRepository.kt`: chamadas a `LauncherApps`, callback e favoritos.
- `LauncherModels.kt`: três modelos simples.
- `LauncherTheme.kt`: Material 3 com cores dinâmicas e fallbacks claro/escuro.

## Compilar

O projeto usa JDK 17, Android SDK 37.0, AGP 9.1.1, Gradle 9.3.1, Kotlin integrado do AGP e Compose Compiler 2.3.21. O Material 3 está fixado em `1.5.0-alpha24` para usar os List Items atuais com suporte nativo a clique e long press.

Na máquina em que o projeto foi gerado, a toolchain de linha de comando já foi instalada. Para compilar:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew assembleDebug
```

O APK é gerado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Instalar pelo Android Studio no Galaxy S24 Ultra

1. Instale o Android Studio Panda 3 (2025.3.3 Patch 1) ou mais recente e abra esta pasta como projeto.
2. Em **Settings > Build, Execution, Deployment > Build Tools > Gradle**, escolha **Gradle JDK 17**. O JDK embutido do Android Studio também serve se for 17 ou superior.
3. Em **Tools > SDK Manager**, confirme que **Android SDK Platform 37.0** e **Android SDK Platform-Tools** estão instalados. Isso é necessário somente para compilar; o app continua instalável no Android 10 ou superior.
4. No S24 Ultra, abra **Configurações > Sobre o telefone > Informações do software** e toque sete vezes em **Número da compilação**. Confirme o PIN para ativar as opções do desenvolvedor.
5. Volte a **Configurações > Opções do desenvolvedor** e ative **Depuração USB**.
6. Conecte o telefone por USB, aceite a impressão digital RSA no aparelho e selecione o S24 Ultra na barra de dispositivos do Android Studio.
7. Clique em **Run app**. Como há também um intent `LAUNCHER`, o Android Studio consegue abrir o protótipo antes que ele seja definido como tela inicial.

Também é possível instalar o APK pelo terminal:

```bash
/opt/homebrew/share/android-commandlinetools/platform-tools/adb devices
/opt/homebrew/share/android-commandlinetools/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Definir como launcher e testar

1. Abra **Launchly**, arraste para a esquerda para chegar a **Apps** e toque em **Definir** no cartão “Usar como tela inicial”.
2. Confirme **Launchly** no diálogo do Android. Como alternativa, no Samsung: **Configurações > Aplicativos > Escolher aplicativos padrão > Aplicativo de início**.
3. Pressione o botão/gesto **Início**. Na primeira execução, uma orientação compacta mostra os três gestos disponíveis. Ela some assim que um gesto é usado; depois disso, a tela mostra somente o wallpaper e as barras de sistema.
4. Arraste para a esquerda para abrir **Apps**; arraste para a direita a partir do wallpaper para abrir **Favoritos**.
5. Na página central, arraste para cima. A busca deve subir, focar o campo e abrir o teclado.
6. Toque em qualquer app da lista para abri-lo.
7. Pressione um app por alguns instantes. A folha inferior deve mostrar os shortcuts publicados, **Adicionar aos favoritos**, **Informações do app** e **Desinstalar**.
8. Adicione um favorito e navegue para a página à esquerda; ele deve aparecer imediatamente e continuar lá após reiniciar o app.
9. Com o Chrome instalado, busque `anônima`. Se a versão instalada do Chrome publicar esse shortcut em português, **Nova guia anônima** aparecerá e poderá ser aberta diretamente.
10. Adicione ou remova um shortcut dinâmico em um app que ofereça essa função; a lista será recarregada pelo `LauncherApps.Callback` sem reiniciar o launcher.
11. Teste **Informações do app** e **Desinstalar**. O Android sempre exibe a confirmação; apps de sistema ou administrados podem não permitir remoção.

Para voltar ao launcher da Samsung, acesse **Configurações > Aplicativos > Escolher aplicativos padrão > Aplicativo de início > One UI Home**. Faça isso antes de desinstalar o protótipo.

Se o Gradle acusar nomes duplicados como `BuildConfig 2.java` ou `... 4.class`, a pasta está sendo sincronizada enquanto a compilação escreve os intermediários. Mova o projeto para uma pasta local fora do iCloud/Drive, execute `./gradlew clean` e compile novamente; o código-fonte não é a causa desse erro.

## Limites intencionais do protótipo

- Favoritos são locais e simples; não há backup, pastas, widgets ou reordenação por arrastar.
- A dependência atual do Material 3 ainda é alpha; a versão está fixada para que futuras mudanças não quebrem a build inesperadamente.
- Alguns fabricantes ou políticas corporativas escondem apps/perfis da API de launcher.
- O texto e a disponibilidade de shortcuts pertencem a cada app e podem mudar entre versões.

Referências oficiais: [Material 3 para Compose](https://developer.android.com/develop/ui/compose/designsystems/material3), [RoleManager](https://developer.android.com/reference/android/app/role/RoleManager), [LauncherApps](https://developer.android.com/reference/android/content/pm/LauncherApps) e [LauncherApps.ShortcutQuery](https://developer.android.com/reference/android/content/pm/LauncherApps.ShortcutQuery).
