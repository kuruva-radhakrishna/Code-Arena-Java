import TextField from '@mui/material/TextField';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

function InputOutputConsole({ inputValue, onInputChange, outputValue, isOutput = true }) {
  return (
    <Box className="io-console" sx={{ mt: 2 }}>
      <Typography variant="subtitle2" sx={{ mb: 1 }}>Input</Typography>
      <TextField
        className="io-input"
        multiline
        minRows={2}
        maxRows={6}
        fullWidth
        value={inputValue}
        onChange={onInputChange}
        placeholder="Custom input (optional)"
        variant="outlined"
        InputProps={{
          style: {
            fontFamily: 'Fira Mono, Consolas, monospace',
            fontSize: '1rem',
            background: '#f5f7fa',
            borderRadius: 8,
          }
        }}
      />
      <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>{isOutput ? 'Output' : 'Console'}</Typography>
      <TextField
        className="io-output"
        multiline
        minRows={2}
        maxRows={8}
        fullWidth
        value={outputValue}
        InputProps={{
          readOnly: true,
          style: {
            fontFamily: 'Fira Mono, Consolas, monospace',
            fontSize: '1rem',
            background: '#f5f7fa',
            borderRadius: 8,
          }
        }}
        variant="outlined"
      />
    </Box>
  );
}

export default InputOutputConsole; 