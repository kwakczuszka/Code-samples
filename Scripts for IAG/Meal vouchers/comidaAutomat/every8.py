def extract_every_8th_line(input_file, output_file):
    infile = open(input_file, 'r')
    outfile = open(output_file, 'w')
    lines = infile.readlines()
    print(len(lines))
    for i in range(0, len(lines)):
        if i % 9 == 0:
            if(len(lines[i].strip()) == 14):
                outfile.write(lines[i].strip())
                outfile.write("\n")
            elif(len(lines[i].strip()) == 13):
                outfile.write(str(0)+lines[i].strip())
                outfile.write("\n")
            else:
                print("Error: ", lines[i].strip())
    
    infile.close()
    outfile.close()
                
input_file_path = 'numeros.txt'  # Replace with the path to your input text file
output_file_path = 'numbers.txt'  # Replace with the path to your output text file

extract_every_8th_line(input_file_path, output_file_path)
