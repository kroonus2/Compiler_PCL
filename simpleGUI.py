import PySimpleGUI as sg

# Add a touch of color
sg.theme('Dark Grey 10')

# Create settings obj
settings = sg.UserSettings()

# All the stuff inside your window.

layout_l = [
    [sg.Text('Lista de Instruções: ')],
    [sg.Multiline(size=(50, 35), key='-CODE-')],
    [sg.Button('Compilar'), sg.Button('Limpar')]
]

layout_r = [
    [sg.Text('Entradas: ')],
    [sg.Listbox(['I:1/0', 'I:1/1', 'I:1/2', 'I:1/3', 'I:1/4', 'I:1/5',
                'I:1/6', 'I:1/7'], no_scrollbar=True, enable_events=True, s=(25, 16), select_mode=sg.LISTBOX_SELECT_MODE_SINGLE, key='-INPUT-')],
    [sg.Text('Saidas: ')],
    [sg.Listbox(['O:2/0', 'O:2/1', 'O:2/2', 'O:2/3', 'O:2/4', 'O:2/5',
                'O:2/6', 'O:2/7'], no_scrollbar=True, enable_events=True, select_mode=sg.LISTBOX_SELECT_MODE_SINGLE, s=(25, 15), key='-OUTPUT-')],
    [sg.Button("Add", enable_events=True, key="-ADDBTN-"),
     sg.Button("Remover", enable_events=True, key="-REMOVEBTN-")]
]

menu_def = [['File', ['Abrir', 'Salvar',]],
            ['Help', ['Sobre', 'Ajuda']],]

layout = [[sg.Menu(menu_def)],
          [sg.Text('Compilador CLP',  font='_22',
                   justification='c', expand_x=True)],
          [sg.Col(layout_l), sg.Col(layout_r)],
          [sg.Text('Status da Varredura: '), sg.StatusBar('', key='-STATUS-')],
          #   [sg.Text('Variáveis: ')],
          #   [sg.Multiline(s=(80, 10), key='-VARIABLES-')],
          ]


# Create the Window
window = sg.Window('Compilador CLP', layout)
code_list = []

while True:
    event, values = window.read()
    if event == sg.WIN_CLOSED or event == 'Cancelar':  # if user closes window or clicks cancel
        break
    elif event == 'Ajuda':
        file = open("Help.txt")
        Helper = file.read()
        sg.popup_scrolled(Helper, title="Helper", font=(
            "Arial", 12), size=(55, 20))

    elif event == "Sobre":
        file = open("About.txt")
        About = file.read()
        sg.popup_scrolled(About, title="About", font=(
            "Arial", 12), size=(55, 20))

    elif event == 'Abrir':
        file_path = sg.popup_get_file(
            'Selecione um arquivo para abrir', file_types=(("Text Files", "*.txt"),))
        if file_path:
            with open(file_path, 'r') as file:
                code = file.read()
                window['-CODE-'].update(code)

    elif event == 'Salvar':
        code = values['-CODE-']
        if code.strip():  # Check if the code is not empty or contains only whitespace
            file_path = sg.popup_get_file('Salve o arquivo como', file_types=(
                ("Text Files", "*.txt"),), save_as=True)
            if file_path:
                with open(file_path, 'w') as file:
                    file.write(code)
        else:
            sg.popup_error(
                'Nenhum código para salvar. Digite seu código antes de salvar.', title='Erro')

    elif event == '-ADDBTN-':
        selected_item = None
        if values['-INPUT-']:
            selected_item = values['-INPUT-'][0]
        elif values['-OUTPUT-']:
            selected_item = values['-OUTPUT-'][0]

        if selected_item:
            code_list.append(selected_item)
            current_text = window['-CODE-'].get()
            if current_text:
                current_text += '    '  # Add 2 tabs of space if the field is not empty
            current_text += selected_item
            window['-CODE-'].update(current_text)

        window['-INPUT-'].update(set_to_index=-1)
        window['-OUTPUT-'].update(set_to_index=-1)

    elif event == '-REMOVEBTN-':
        if code_list:
            last_item = code_list.pop()
            current_text = window['-CODE-'].get()
            if current_text.endswith(last_item):
                # Remove the last item and 2 tabs of space
                current_text = current_text[:-(len(last_item) + 4)]
                window['-CODE-'].update(current_text)
    elif event == 'Limpar':
        current_text = window['-CODE-'].get()
        if current_text:
            window['-CODE-'].update('')

    elif event == 'Compilar':
        print('Compilando...', values[0])

window.close()
