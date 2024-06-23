' Get the list of all running processes
Dim processes
Set processes = GetObject("winmgmts:\\.\root\cimv2").ExecQuery("SELECT * FROM Win32_Process WHERE Name = 'ssh.exe'")

' Iterate through the processes and check if they have an SSH connection
For Each process In processes
    Dim commandLine
    commandLine = process.CommandLine
        process.Terminate()
Next