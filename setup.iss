; Change these constants to match your system
#define JavaProjectPath "D:\Dropbox\Projects\Behave"
#define SetupOutputPath "C:\Users\ofri\Desktop"
#define VLCPath "D:\Program Files (x86)\VideoLAN\VLC"

; --------------------------------------------------------------------------

#define MyAppName "Behave Video Scoring"
#define MyAppVersion "0.1"
#define MyAppPublisher "Ofri Mann"
#define MyAppExeName "Behave.exe"

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{9FC88921-0794-402C-8FC4-A6E3CAE4CF9C}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
;AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={pf}\Behave
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
OutputDir={#SetupOutputPath}
OutputBaseFilename=setup
Compression=lzma
SolidCompression=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "quicklaunchicon"; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked; OnlyBelowVersion: 0,6.1

[Files]
Source: "{#JavaProjectPath}\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#VLCPath}\libvlc.dll"; DestDir: "{app}\vlc"; Flags: ignoreversion
Source: "{#VLCPath}\libvlccore.dll"; DestDir: "{app}\vlc"; Flags: ignoreversion
Source: "{#VLCPath}\plugins\*"; DestDir: "{app}\vlc\plugins"; Excludes: "\gui,\lua"; Flags: ignoreversion recursesubdirs createallsubdirs
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{commondesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon
Name: "{userappdata}\Microsoft\Internet Explorer\Quick Launch\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: quicklaunchicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: shellexec postinstall skipifsilent
