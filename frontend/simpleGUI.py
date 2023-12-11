import psutil
import PySimpleGUI as sg
from modBusComunication import Modbus
from modbusConfigManager import ModbusConfigManager
import serial.tools.list_ports
import tempfile
from subprocess import Popen, PIPE, STDOUT
import time
import threading

# Add a touch of color
sg.theme('Dark Grey 10')

# Create settings obj
settings = sg.UserSettings()
output_reg = [0, 0, 0, 0, 0, 0, 0, 0]

# Create global vars
selected_port = ""
scan_cycle_time = 1
last_process = last_thread = is_initialize = False

modbus = Modbus()


config_manager = ModbusConfigManager()
configuracoes_salvas = config_manager.carregar_configuracoes()

if configuracoes_salvas:
    output_reg = configuracoes_salvas.get('OUTPUT', output_reg)


def list_serial_ports():
    serial_ports = list(serial.tools.list_ports.comports())
    port_names = [port.device for port in serial_ports]
    return port_names


def create_file(source_code):
    with tempfile.TemporaryFile(suffix='.pas', mode='w+', encoding='utf-8', delete=False) as code_file:
        code_file.write(source_code)
        code_file.seek(0)
        return code_file.name


def kill_process(process):
    parent_process = psutil.Process(process.pid)
    for child_process in parent_process.children(recursive=True):
        child_process.terminate()
    return True


def scan_cycle():
    while last_thread:
        print("Entrou no Ciclo")
        modbus.get_instrument(selected_port)
        modbus.read_input_registers()
        print("Leu as entradas")
        configuracoes_salvas = config_manager.carregar_configuracoes()  # aq
        output_reg = configuracoes_salvas.get('OUTPUT')
        modbus.write_output_registers(output_reg)
        print(f"Escreveu nas saidas --> {output_reg}")
        config_manager.atualizar_input_reg(modbus.input_reg)
        update_IN_OUT(configuracoes_salvas)
        print("Atualizou a tela")
        time.sleep(scan_cycle_time)


def update_IN_OUT(configuracoes_salvas):
    window['-INPUT-'].Update(
        [f'I1  --  {modbus.input_reg[7]}',
         f'I2  --  {modbus.input_reg[6]}',
         f'I3  --  {modbus.input_reg[5]}',
         f'I4  --  {modbus.input_reg[4]}',
         f'I5  --  {modbus.input_reg[3]}',
         f'I6  --  {modbus.input_reg[2]}',
         f'I7  --  {modbus.input_reg[1]}',
         f'I8  --  {modbus.input_reg[0]}']
    )
    window['-OUTPUT-'].Update(
        [f'Q1  --  {configuracoes_salvas.get("OUTPUT")[7]}',
         f'Q2  --  {configuracoes_salvas.get("OUTPUT")[6]}',
         f'Q3  --  {configuracoes_salvas.get("OUTPUT")[5]}',
         f'Q4  --  {configuracoes_salvas.get("OUTPUT")[4]}',
         f'Q5  --  {configuracoes_salvas.get("OUTPUT")[3]}',
         f'Q6  --  {configuracoes_salvas.get("OUTPUT")[2]}',
         f'Q7  --  {configuracoes_salvas.get("OUTPUT")[1]}',
         f'Q8  --  {configuracoes_salvas.get("OUTPUT")[0]}']
    )


# All the stuff inside your window.
layout_l = [
    [sg.Text('Lista de Instruções: ')],
    [sg.Multiline(size=(50, 33), key='-CODE-')],
    [sg.Button('Executar', disabled=False, key='-RUN-'), sg.Button(
        'Parar', disabled=True, key='-STOP-', button_color=('white', '#800000')), sg.Button('Limpar', key='-CLEAR-')]
    # Botão "Parar" desabilitado inicialmente
]

layout_r = [
    [sg.Text('Porta Serial:'), sg.Combo(list_serial_ports(),
                                        key='-CHOOSE_PORT-', enable_events=True)],
    [sg.Text('Entradas: ')],
    [sg.Listbox([f'I1  --  {modbus.input_reg[7]}',
                 f'I2  --  {modbus.input_reg[6]}',
                 f'I3  --  {modbus.input_reg[5]} ',
                 f'I4  --  {modbus.input_reg[4]}',
                 f'I5  --  {modbus.input_reg[3]}',
                 f'I6  --  {modbus.input_reg[2]}',
                 f'I7  --  {modbus.input_reg[1]}',
                 f'I8  --  {modbus.input_reg[0]}'],
                no_scrollbar=True, enable_events=True, s=(25, 11), select_mode=sg.LISTBOX_SELECT_MODE_SINGLE, key='-INPUT-')],
    [sg.Text('Saidas: ')],
    [sg.Listbox([f'Q1  --  {output_reg[7]}',
                 f'Q2  --  {output_reg[6]}',
                 f'Q3  --  {output_reg[5]}',
                 f'Q4  --  {output_reg[4]}',
                 f'Q5  --  {output_reg[3]}',
                 f'Q6  --  {output_reg[2]}',
                 f'Q7  --  {output_reg[1]}',
                 f'Q8  --  {output_reg[0]}'],
                no_scrollbar=True, enable_events=True, select_mode=sg.LISTBOX_SELECT_MODE_SINGLE, s=(25, 11), key='-OUTPUT-')],
    [sg.Text('Tempo APÓS varredura(s): ')],
    [sg.Input(key='-TIMEREAD-', s=(12, 1)),
     sg.Button("Ok", enable_events=True, key="-OKBTN-")],
]

menu_def = [['File', ['Abrir', 'Salvar',]],
            ['Help', ['Sobre', 'Ajuda']],]

layout = [[sg.Menu(menu_def)],
          [sg.Text('Compilador CLP',  font='_22',
                   justification='c', expand_x=True)],
          [sg.Col(layout_l), sg.Col(layout_r)],
          ]


# Create the Window
window = sg.Window('Compilador CLP', layout)
code_list = []

while True:
    event, values = window.read()
    if event == sg.WIN_CLOSED or event == 'Cancelar':  # if user closes window or clicks cancel
        break

    elif event == '-CHOOSE_PORT-':
        selected_port = values['-CHOOSE_PORT-']
        if selected_port.upper() == 'COM1':
            sg.popup_error(
                'A porta COM1 não é válida. Por favor, escolha outra porta.', title='Erro')
        else:
            window['-INPUT-'].Update(
                [f'I1  --  {modbus.input_reg[7]}',
                 f'I2  --  {modbus.input_reg[6]}',
                 f'I3  --  {modbus.input_reg[5]}',
                 f'I4  --  {modbus.input_reg[4]}',
                 f'I5  --  {modbus.input_reg[3]}',
                 f'I6  --  {modbus.input_reg[2]}',
                 f'I7  --  {modbus.input_reg[1]}',
                 f'I8  --  {modbus.input_reg[0]}']
            )
        if selected_port and selected_port.upper() != 'COM1':
            sg.popup(
                f'Porta Selecionada: {selected_port}', title='Porta Selecionada')

    elif event == 'Ajuda':
        file = open("frontend/Help.txt")
        Helper = file.read()
        sg.popup_scrolled(Helper, title="Helper", font=(
            "Arial", 12), size=(55, 20))

    elif event == "Sobre":
        file = open("frontend/About.txt")
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

    elif event == '-OKBTN-':
        try:
            scan_cycle_time = int(values['-TIMEREAD-'])
        except ValueError:
            scan_cycle_time = 1
            sg.popup_error(
                'Por favor, insira um valor válido para o tempo após a varredura.', title='Erro')

    elif event == '-RUN-':
        if not selected_port or selected_port.upper() == 'COM1':
            sg.popup_error(
                'Por favor, selecione uma porta válida antes de executar o código.', title='Erro na Porta')
            continue  # Volta ao início do loop, impedindo a execução do código
        code = values['-CODE-']
        if not code.strip():  # Check if the code is not empty or contains only whitespace
            sg.popup_error(
                'Não há código a ser executado.', title='Erro na Sintaxe')
            continue  # Volta ao início do loop, impedindo a execução do código

        is_initialize = True
        if (is_initialize):
            window['-RUN-'].Update(disabled=True)
            window['-STOP-'].Update(disabled=False)
            window['-CODE-'].Update(disabled=True)

        source_code = str(window['-CODE-'].get()+'\n')
        path_source_code = create_file(source_code)

        last_process = Popen(["java", "-cp", "../backend/target/compilador-clp-1.0-SNAPSHOT-jar-with-dependencies.jar",
                             "iftm.compilador.clp.App", path_source_code])

        last_thread = threading.Thread(target=scan_cycle)
        last_thread.start()

    elif event == "-STOP-":
        is_initialize = False
        last_thread = False
        window['-CODE-'].Update(disabled=False)
        kill_process(last_process)
        last_process = False
        
        window['-RUN-'].Update(disabled=False)
        window['-STOP-'].Update(disabled=True)

        sg.popup_ok(
                'O programa foi parado com sucesso.', title='Parado com sucesso')

    elif event == "-CLEAR-":
        window['-CODE-'].Update("")

window.close()
